package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PeriodPalette")
data class PeriodPaletteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodType: String, // WEEK, MONTH, SEASON, YEAR
    val startDate: Long, // epoch days
    val endDate: Long, // epoch days
    val colors: String, // JSON array of hex strings
    val photoCount: Int,
    val dominantColor: String, // hex string
    val poeticDescription: String,
)
