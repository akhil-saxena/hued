package app.hued.processing

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

@HiltWorker
class ProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val galleryScanner: GalleryScanner,
    private val paletteExtractor: PaletteExtractor,
    private val colorAggregator: ColorAggregator,
    private val paletteRepository: PaletteRepository,
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val checkpoint = paletteRepository.getCheckpoint()
        val sinceTimestamp = checkpoint?.lastTimestamp ?: 0L
        val excludedFolders = paletteRepository.getExcludedFolders()

        val images = galleryScanner.scanGallery(excludedFolders, sinceTimestamp)
        if (images.isEmpty()) return Result.success()

        for (image in images) {
            val extracted = paletteExtractor.extract(image.uri) ?: continue
            val entity = PaletteResultEntity(
                imageUri = image.uri.toString(),
                timestamp = image.timestamp,
                colors = json.encodeToString(extracted.hexColors),
                folderPath = image.folderPath,
            )
            paletteRepository.savePaletteResult(entity)
        }

        // Re-aggregate current week
        aggregateCurrentWeek()

        return Result.success()
    }

    private suspend fun aggregateCurrentWeek() {
        val now = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfWeek = now.with(weekFields.dayOfWeek(), 1)
        val endOfWeek = startOfWeek.plusDays(7)

        val startTimestamp = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTimestamp = endOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val results = paletteRepository.getResultsForPeriod(startTimestamp, endTimestamp)
        if (results.isNotEmpty()) {
            val palette = colorAggregator.aggregate(results, TimePeriod.WEEK, startOfWeek, endOfWeek)
            paletteRepository.savePalette(palette)
        }
    }
}
