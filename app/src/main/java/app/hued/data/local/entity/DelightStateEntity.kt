package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DelightState")
data class DelightStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // MONOCHROME, HARMONY, BIRTHDAY, NEW_YEAR
    val periodKey: String,
    val shown: Boolean,
)
