package app.hued.data.repository

import app.hued.data.local.entity.StreakDataEntity
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    fun getActiveStreaks(): Flow<List<StreakDataEntity>>
    fun getAllStreaks(): Flow<List<StreakDataEntity>>
    suspend fun saveStreak(streak: StreakDataEntity)
    suspend fun deactivateStreak(id: Long)
}
