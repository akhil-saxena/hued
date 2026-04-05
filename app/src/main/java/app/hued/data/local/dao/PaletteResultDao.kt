package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.ColumnInfo
import app.hued.data.local.entity.PaletteResultEntity
import kotlinx.coroutines.flow.Flow

data class FolderCount(
    @ColumnInfo(name = "folderPath") val folderPath: String,
    @ColumnInfo(name = "photoCount") val photoCount: Int,
)

@Dao
interface PaletteResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: PaletteResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<PaletteResultEntity>)

    @Query("SELECT * FROM PaletteResult WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp ORDER BY timestamp DESC")
    suspend fun getResultsForPeriod(startTimestamp: Long, endTimestamp: Long): List<PaletteResultEntity>

    @Query("SELECT * FROM PaletteResult WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp AND folderPath NOT IN (:excludedFolders) ORDER BY timestamp DESC")
    suspend fun getResultsForPeriodExcluding(
        startTimestamp: Long,
        endTimestamp: Long,
        excludedFolders: List<String>,
    ): List<PaletteResultEntity>

    @Query("SELECT COUNT(*) FROM PaletteResult")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT folderPath, COUNT(*) as photoCount FROM PaletteResult GROUP BY folderPath ORDER BY photoCount DESC")
    suspend fun getFolderCounts(): List<FolderCount>

    @Query("DELETE FROM PaletteResult")
    suspend fun deleteAll()
}
