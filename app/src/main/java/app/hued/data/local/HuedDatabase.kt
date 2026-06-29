package app.hued.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HuedDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE PeriodPalette ADD COLUMN colorWeights TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ProcessingCheckpoint ADD COLUMN currentYearDone INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // DATE_ADDED watermark so incremental scans don't reprocess the whole gallery
                db.execSQL("ALTER TABLE ProcessingCheckpoint ADD COLUMN lastDateAdded INTEGER NOT NULL DEFAULT 0")
                // Earlier versions could insert the same image many times (no unique key on imageUri),
                // double-counting colors. Collapse duplicates (keep the earliest row) before enforcing uniqueness.
                db.execSQL(
                    "DELETE FROM PaletteResult WHERE id NOT IN " +
                        "(SELECT MIN(id) FROM PaletteResult GROUP BY imageUri)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_PaletteResult_imageUri " +
                        "ON PaletteResult(imageUri)",
                )
            }
        }
    }

    abstract fun paletteResultDao(): PaletteResultDao
    abstract fun periodPaletteDao(): PeriodPaletteDao
    abstract fun streakDao(): StreakDao
    abstract fun delightStateDao(): DelightStateDao
    abstract fun processingCheckpointDao(): ProcessingCheckpointDao
    abstract fun excludedFolderDao(): ExcludedFolderDao
}
