package app.hued.processing

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.hued.data.DevToolsSettingsProvider
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.util.DateUtils
import app.hued.widget.HuedWidget
import androidx.glance.appwidget.updateAll
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class ProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryScanner: GalleryScanner,
    private val paletteExtractor: PaletteExtractor,
    private val colorAggregator: ColorAggregator,
    private val paletteRepository: PaletteRepository,
    private val devToolsSettingsProvider: DevToolsSettingsProvider,
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val settings = devToolsSettingsProvider.getCurrent()
        val paletteDepth = settings.paletteDepth

        val checkpoint = paletteRepository.getCheckpoint()
        val sinceDateAdded = checkpoint?.lastDateAdded ?: 0L
        val excludedFolders = paletteRepository.getExcludedFolders()

        val images = galleryScanner.scanGallery(excludedFolders, sinceDateAdded)
        if (images.isEmpty()) return Result.success()

        for (image in images) {
            val extracted = paletteExtractor.extract(image.uri, paletteDepth) ?: continue
            val entity = PaletteResultEntity(
                imageUri = image.uri.toString(),
                timestamp = image.timestamp,
                colors = json.encodeToString(extracted.hexColors),
                folderPath = image.folderPath,
            )
            paletteRepository.savePaletteResult(entity)
        }

        // Advance the watermark so we don't re-scan these next week
        val maxDateAdded = images.maxOfOrNull { it.dateAdded } ?: 0L
        if (checkpoint != null && maxDateAdded > checkpoint.lastDateAdded) {
            paletteRepository.saveCheckpoint(checkpoint.copy(lastDateAdded = maxDateAdded))
        }

        // Re-aggregate current week + refresh the widget
        aggregateCurrentWeek(paletteDepth)
        try {
            HuedWidget().updateAll(applicationContext)
        } catch (e: Exception) {
            android.util.Log.w("ProcessingWorker", "Widget refresh failed", e)
        }

        return Result.success()
    }

    private suspend fun aggregateCurrentWeek(maxColors: Int) {
        val now = LocalDate.now()
        val startOfWeek = DateUtils.startOfWeek(now)
        val endOfWeek = startOfWeek.plusDays(7)

        val startTimestamp = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTimestamp = endOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val results = paletteRepository.getResultsForPeriod(startTimestamp, endTimestamp)
        if (results.isNotEmpty()) {
            val palette = colorAggregator.aggregate(results, TimePeriod.WEEK, startOfWeek, endOfWeek, maxColors)
            paletteRepository.deletePaletteForPeriod(TimePeriod.WEEK, startOfWeek.toEpochDay())
            paletteRepository.savePalette(palette)
        }
    }
}
