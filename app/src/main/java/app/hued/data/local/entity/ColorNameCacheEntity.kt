package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ColorNameCache")
data class ColorNameCacheEntity(
    @PrimaryKey val hexValue: String,
    val colorName: String,
)
