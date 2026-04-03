package app.hued.data.repository

import app.hued.data.local.dao.DelightStateDao
import app.hued.data.local.entity.DelightStateEntity
import javax.inject.Inject

class DelightRepositoryImpl @Inject constructor(
    private val delightStateDao: DelightStateDao,
) : DelightRepository {

    override suspend fun getState(type: String, periodKey: String): DelightStateEntity? =
        delightStateDao.getState(type, periodKey)

    override suspend fun getUnshownMoments(): List<DelightStateEntity> =
        delightStateDao.getUnshownMoments()

    override suspend fun saveState(state: DelightStateEntity) =
        delightStateDao.insert(state)

    override suspend fun markShown(id: Long) =
        delightStateDao.markShown(id)
}
