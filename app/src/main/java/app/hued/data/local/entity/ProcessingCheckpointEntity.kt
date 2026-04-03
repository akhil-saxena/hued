package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProcessingCheckpoint")
data class ProcessingCheckpointEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val lastMediaStoreId: Long,
    val lastTimestamp: Long,
    val totalProcessed: Int,
    val totalFound: Int,
    val isComplete: Boolean,
)
