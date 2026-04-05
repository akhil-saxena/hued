package app.hued.data.repository

import app.hued.data.local.dao.FolderCount
import app.hued.data.local.entity.ExcludedFolderEntity
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.local.entity.ProcessingCheckpointEntity
import app.hued.data.model.TimePeriod
import kotlinx.coroutines.flow.Flow

interface PaletteRepository {

    fun getAllPalettes(type: TimePeriod): Flow<List<PeriodPaletteEntity>>

    fun getLatestPalette(type: TimePeriod): Flow<PeriodPaletteEntity?>

    suspend fun getPaletteForPeriod(type: TimePeriod, startEpochDay: Long, endEpochDay: Long): PeriodPaletteEntity?

    suspend fun deleteAllPalettes()

    suspend fun deletePaletteForPeriod(type: TimePeriod, startEpochDay: Long)

    suspend fun savePalette(palette: PeriodPaletteEntity)

    suspend fun savePaletteResult(result: PaletteResultEntity)

    suspend fun savePaletteResults(results: List<PaletteResultEntity>)

    suspend fun getResultsForPeriod(startTimestamp: Long, endTimestamp: Long): List<PaletteResultEntity>

    suspend fun getExcludedFolders(): List<String>

    suspend fun getFavoriteColor(): String?

    fun getTotalResultCount(): Flow<Int>

    suspend fun getCheckpoint(): ProcessingCheckpointEntity?

    fun observeCheckpoint(): Flow<ProcessingCheckpointEntity?>

    suspend fun saveCheckpoint(checkpoint: ProcessingCheckpointEntity)

    suspend fun clearCheckpoint()

    suspend fun getPhotoCountForPeriod(startEpochDay: Long, endEpochDay: Long): Int

    suspend fun deleteAllResults()

    suspend fun getFolderCounts(): List<FolderCount>

    fun observeExcludedFolders(): Flow<List<ExcludedFolderEntity>>

    suspend fun addExcludedFolder(path: String)

    suspend fun removeExcludedFolder(path: String)
}
