package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PaletteResult")
data class PaletteResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val timestamp: Long,
    val colors: String, // JSON array of hex strings
    val folderPath: String,
)
