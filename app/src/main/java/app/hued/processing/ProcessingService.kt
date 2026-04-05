package app.hued.processing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.hued.R
import app.hued.data.DevToolsSettingsProvider
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.ProcessingCheckpointEntity
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.di.DefaultDispatcher
import app.hued.di.IoDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProcessingService : Service() {

    @Inject lateinit var galleryScanner: GalleryScanner
    @Inject lateinit var paletteExtractor: PaletteExtractor
    @Inject lateinit var colorAggregator: ColorAggregator
    @Inject lateinit var paletteRepository: PaletteRepository
    @Inject lateinit var devToolsSettingsProvider: DevToolsSettingsProvider
    @Inject @DefaultDispatcher lateinit var defaultDispatcher: CoroutineDispatcher
    @Inject @IoDispatcher lateinit var ioDispatcher: CoroutineDispatcher

    private val serviceScope = CoroutineScope(SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val CHANNEL_ID = "hued_processing"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Building your color history..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            processGallery()
            stopSelf()
        }
        return START_STICKY
    }

    private var paletteDepth: Int = 5

    private suspend fun processGallery() {
        paletteDepth = devToolsSettingsProvider.getCurrent().paletteDepth

        // On first run, pre-exclude known junk folders
        val checkpoint = withContext(ioDispatcher) {
            paletteRepository.getCheckpoint()
        }
        if (checkpoint == null) {
            withContext(ioDispatcher) {
                app.hued.data.model.DEFAULT_EXCLUDED_FOLDERS.forEach { folder ->
                    paletteRepository.addExcludedFolder(folder)
                }
            }
        }

        val excludedFolders = withContext(ioDispatcher) {
            paletteRepository.getExcludedFolders()
        }

        val sinceTimestamp = checkpoint?.lastTimestamp ?: 0L

        val images = withContext(ioDispatcher) {
            galleryScanner.scanGallery(excludedFolders, sinceTimestamp)
        }

        if (images.isEmpty()) return

        val alreadyProcessed = checkpoint?.totalProcessed ?: 0
        val totalFound = images.size + alreadyProcessed
        var totalProcessed = alreadyProcessed

        for ((index, image) in images.withIndex()) {
            val extracted = withContext(defaultDispatcher) {
                paletteExtractor.extract(image.uri, paletteDepth)
            } ?: continue

            val entity = PaletteResultEntity(
                imageUri = image.uri.toString(),
                timestamp = image.timestamp,
                colors = json.encodeToString(extracted.hexColors),
                folderPath = image.folderPath,
            )

            withContext(ioDispatcher) {
                paletteRepository.savePaletteResult(entity)
            }

            totalProcessed++

            // Save checkpoint every 5 images for progress tracking
            if (totalProcessed % 5 == 0 || index == images.lastIndex) {
                withContext(ioDispatcher) {
                    paletteRepository.saveCheckpoint(
                        ProcessingCheckpointEntity(
                            lastMediaStoreId = 0,
                            lastTimestamp = image.timestamp,
                            totalProcessed = totalProcessed,
                            totalFound = totalFound,
                            isComplete = index == images.lastIndex,
                        )
                    )
                }

                updateNotification("Building your color history... ${totalProcessed}/${totalFound}")
            }

            // Aggregate periodically (every 100 images) and always at the end
            if (totalProcessed % 100 == 0 || index == images.lastIndex) {
                aggregateAllPeriods()
            }
        }
    }

    private suspend fun aggregateAllPeriods() {
        // Each period is upserted individually in aggregatePeriod() — no bulk delete needed
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // Aggregate WEEKS — go back 52 weeks
        var weekStart = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
        repeat(52) {
            val weekEnd = weekStart.plusDays(7)
            aggregatePeriod(TimePeriod.WEEK, weekStart, weekEnd, zone)
            weekStart = weekStart.minusWeeks(1)
        }

        // Aggregate MONTHS — go back 24 months
        var monthStart = now.withDayOfMonth(1)
        repeat(24) {
            val monthEnd = monthStart.plusMonths(1)
            aggregatePeriod(TimePeriod.MONTH, monthStart, monthEnd, zone)
            monthStart = monthStart.minusMonths(1)
        }

        // Aggregate YEARS — go back 3 years
        var yearStart = LocalDate.of(now.year, 1, 1)
        repeat(3) {
            val yearEnd = yearStart.plusYears(1)
            aggregatePeriod(TimePeriod.YEAR, yearStart, yearEnd, zone)
            yearStart = yearStart.minusYears(1)
        }
    }

    private suspend fun aggregatePeriod(
        type: TimePeriod,
        start: LocalDate,
        end: LocalDate,
        zone: ZoneId,
    ) {
        val startTimestamp = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endTimestamp = end.atStartOfDay(zone).toInstant().toEpochMilli()

        val results = withContext(ioDispatcher) {
            paletteRepository.getResultsForPeriod(startTimestamp, endTimestamp)
        }

        if (results.isNotEmpty()) {
            val palette = withContext(defaultDispatcher) {
                colorAggregator.aggregate(results, type, start, end, paletteDepth)
            }
            withContext(ioDispatcher) {
                // Delete existing row for this period before inserting to avoid duplicates
                paletteRepository.deletePaletteForPeriod(type, start.toEpochDay())
                paletteRepository.savePalette(palette)
            }
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_share) // placeholder icon
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Processing",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Gallery processing progress"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
