package app.hued.data.repository

import app.hued.data.local.dao.StreakDao
import app.hued.data.local.entity.StreakDataEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
) : StreakRepository {

    override fun getActiveStreaks(): Flow<List<StreakDataEntity>> =
        streakDao.getActiveStreaks()

    override fun getAllStreaks(): Flow<List<StreakDataEntity>> =
        streakDao.getAllStreaks()

    override suspend fun saveStreak(streak: StreakDataEntity) =
        streakDao.insert(streak)

    override suspend fun deactivateStreak(id: Long) =
        streakDao.deactivateStreak(id)
}
