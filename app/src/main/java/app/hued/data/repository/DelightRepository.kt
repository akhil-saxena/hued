package app.hued.data.repository

import app.hued.data.local.entity.DelightStateEntity

interface DelightRepository {
    suspend fun getState(type: String, periodKey: String): DelightStateEntity?
    suspend fun getUnshownMoments(): List<DelightStateEntity>
    suspend fun saveState(state: DelightStateEntity)
    suspend fun markShown(id: Long)
}
