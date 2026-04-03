package app.hued.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ExcludedFolder")
data class ExcludedFolderEntity(
    @PrimaryKey val folderPath: String,
)
