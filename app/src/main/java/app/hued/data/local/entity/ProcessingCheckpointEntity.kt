package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProcessingCheckpoint")
data class ProcessingCheckpointEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val lastMediaStoreId: Long,
    val lastTimestamp: Long, // DATE_TAKEN of last processed image (used for bucketing/labels)
    val totalProcessed: Int,
    val totalFound: Int,
    val isComplete: Boolean,
    val currentYearDone: Boolean = false,
    val lastDateAdded: Long = 0, // DATE_ADDED watermark (millis) — incremental scans only fetch newer photos
)
