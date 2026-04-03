package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StreakData")
data class StreakDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: Long, // epoch days
    val endDate: Long, // epoch days
    val toneFamily: String,
    val dayCount: Int,
    val isActive: Boolean,
)
