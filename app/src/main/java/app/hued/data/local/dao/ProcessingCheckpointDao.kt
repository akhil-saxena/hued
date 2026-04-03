package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.hued.data.local.entity.ProcessingCheckpointEntity

@Dao
interface ProcessingCheckpointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(checkpoint: ProcessingCheckpointEntity)

    @Query("SELECT * FROM ProcessingCheckpoint WHERE id = 1 LIMIT 1")
    suspend fun getCheckpoint(): ProcessingCheckpointEntity?

    @Query("DELETE FROM ProcessingCheckpoint")
    suspend fun clear()
}
