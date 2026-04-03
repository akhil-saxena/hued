package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.hued.data.local.entity.DelightStateEntity

@Dao
interface DelightStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: DelightStateEntity)

    @Query("SELECT * FROM DelightState WHERE type = :type AND periodKey = :periodKey LIMIT 1")
    suspend fun getState(type: String, periodKey: String): DelightStateEntity?

    @Query("SELECT * FROM DelightState WHERE shown = 0")
    suspend fun getUnshownMoments(): List<DelightStateEntity>

    @Query("UPDATE DelightState SET shown = 1 WHERE id = :id")
    suspend fun markShown(id: Long)
}
