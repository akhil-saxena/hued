package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.hued.data.local.entity.StreakDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(streak: StreakDataEntity)

    @Query("SELECT * FROM StreakData WHERE isActive = 1 ORDER BY dayCount DESC")
    fun getActiveStreaks(): Flow<List<StreakDataEntity>>

    @Query("SELECT * FROM StreakData ORDER BY dayCount DESC")
    fun getAllStreaks(): Flow<List<StreakDataEntity>>

    @Query("UPDATE StreakData SET isActive = 0 WHERE id = :id")
    suspend fun deactivateStreak(id: Long)

    @Query("DELETE FROM StreakData")
    suspend fun deleteAll()
}
