package app.hued.data.repository

import app.hued.data.local.dao.ExcludedFolderDao
import app.hued.data.local.dao.FolderCount
import app.hued.data.local.dao.PaletteResultDao
import app.hued.data.local.dao.PeriodPaletteDao
import app.hued.data.local.dao.ProcessingCheckpointDao
import app.hued.data.local.entity.ExcludedFolderEntity
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

    override suspend fun deleteAllPalettes() =
        periodPaletteDao.deleteAll()

    override suspend fun deletePaletteForPeriod(type: TimePeriod, startEpochDay: Long) =
        periodPaletteDao.deleteForPeriod(type.name, startEpochDay)

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

    override fun observeCheckpoint(): Flow<ProcessingCheckpointEntity?> =
        processingCheckpointDao.observeCheckpoint()

    override suspend fun saveCheckpoint(checkpoint: ProcessingCheckpointEntity) =
        processingCheckpointDao.save(checkpoint)

    override suspend fun clearCheckpoint() =
        processingCheckpointDao.clear()

    override suspend fun getPhotoCountForPeriod(startEpochDay: Long, endEpochDay: Long): Int =
        periodPaletteDao.getPhotoCountForPeriod(startEpochDay, endEpochDay)

    override suspend fun deleteAllResults() =
        paletteResultDao.deleteAll()

    override suspend fun getFolderCounts(): List<FolderCount> =
        paletteResultDao.getFolderCounts()

    override fun observeExcludedFolders(): Flow<List<ExcludedFolderEntity>> =
        excludedFolderDao.getAll()

    override suspend fun addExcludedFolder(path: String) =
        excludedFolderDao.insert(ExcludedFolderEntity(path))

    override suspend fun removeExcludedFolder(path: String) =
        excludedFolderDao.delete(path)
}
