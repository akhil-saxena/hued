package app.hued.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.hued.data.local.dao.DelightStateDao
import app.hued.data.local.dao.ExcludedFolderDao
import app.hued.data.local.dao.PaletteResultDao
import app.hued.data.local.dao.PeriodPaletteDao
import app.hued.data.local.dao.ProcessingCheckpointDao
import app.hued.data.local.dao.StreakDao
import app.hued.data.local.entity.ColorNameCacheEntity
import app.hued.data.local.entity.DelightStateEntity
import app.hued.data.local.entity.ExcludedFolderEntity
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.local.entity.ProcessingCheckpointEntity
import app.hued.data.local.entity.StreakDataEntity

@Database(
    entities = [
        PaletteResultEntity::class,
        PeriodPaletteEntity::class,
        ColorNameCacheEntity::class,
        StreakDataEntity::class,
        DelightStateEntity::class,
        ProcessingCheckpointEntity::class,
        ExcludedFolderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HuedDatabase : RoomDatabase() {
    abstract fun paletteResultDao(): PaletteResultDao
    abstract fun periodPaletteDao(): PeriodPaletteDao
    abstract fun streakDao(): StreakDao
    abstract fun delightStateDao(): DelightStateDao
    abstract fun processingCheckpointDao(): ProcessingCheckpointDao
    abstract fun excludedFolderDao(): ExcludedFolderDao
}
