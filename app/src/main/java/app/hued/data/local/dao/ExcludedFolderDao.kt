package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.hued.data.local.entity.ExcludedFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcludedFolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: ExcludedFolderEntity)

    @Query("SELECT * FROM ExcludedFolder")
    fun getAll(): Flow<List<ExcludedFolderEntity>>

    @Query("SELECT folderPath FROM ExcludedFolder")
    suspend fun getExcludedPaths(): List<String>

    @Query("DELETE FROM ExcludedFolder WHERE folderPath = :path")
    suspend fun delete(path: String)
}
