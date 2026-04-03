package app.hued.data.repository

import app.hued.data.local.dao.ExcludedFolderDao
import app.hued.data.local.dao.PaletteResultDao
import app.hued.data.local.dao.PeriodPaletteDao
import app.hued.data.local.dao.ProcessingCheckpointDao
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.local.entity.ProcessingCheckpointEntity
import app.hued.data.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PaletteRepositoryImpl @Inject constructor(
    private val paletteResultDao: PaletteResultDao,
    private val periodPaletteDao: PeriodPaletteDao,
    private val processingCheckpointDao: ProcessingCheckpointDao,
    private val excludedFolderDao: ExcludedFolderDao,
) : PaletteRepository {

    override fun getAllPalettes(type: TimePeriod): Flow<List<PeriodPaletteEntity>> =
        periodPaletteDao.getAllByType(type.name)

    override fun getLatestPalette(type: TimePeriod): Flow<PeriodPaletteEntity?> =
        periodPaletteDao.getLatestByType(type.name)

    override suspend fun getPaletteForPeriod(
        type: TimePeriod,
        startEpochDay: Long,
        endEpochDay: Long,
    ): PeriodPaletteEntity? =
        periodPaletteDao.getForPeriod(type.name, startEpochDay, endEpochDay)

    override suspend fun savePalette(palette: PeriodPaletteEntity) =
        periodPaletteDao.insert(palette)

    override suspend fun savePaletteResult(result: PaletteResultEntity) =
        paletteResultDao.insert(result)

    override suspend fun savePaletteResults(results: List<PaletteResultEntity>) =
        paletteResultDao.insertAll(results)

    override suspend fun getResultsForPeriod(
        startTimestamp: Long,
        endTimestamp: Long,
    ): List<PaletteResultEntity> =
        paletteResultDao.getResultsForPeriod(startTimestamp, endTimestamp)

    override suspend fun getExcludedFolders(): List<String> =
        excludedFolderDao.getExcludedPaths()

    override suspend fun getFavoriteColor(): String? =
        periodPaletteDao.getFavoriteColor()

    override fun getTotalResultCount(): Flow<Int> =
        paletteResultDao.getTotalCount()

    override suspend fun getCheckpoint(): ProcessingCheckpointEntity? =
        processingCheckpointDao.getCheckpoint()

    override suspend fun saveCheckpoint(checkpoint: ProcessingCheckpointEntity) =
        processingCheckpointDao.save(checkpoint)

    override suspend fun clearCheckpoint() =
        processingCheckpointDao.clear()

    override suspend fun getPhotoCountForPeriod(startEpochDay: Long, endEpochDay: Long): Int =
        periodPaletteDao.getPhotoCountForPeriod(startEpochDay, endEpochDay)
}
