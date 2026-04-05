package app.hued.di

import android.content.Context
import androidx.room.Room
import app.hued.data.local.HuedDatabase
import app.hued.data.local.dao.DelightStateDao
import app.hued.data.local.dao.ExcludedFolderDao
import app.hued.data.local.dao.PaletteResultDao
import app.hued.data.local.dao.PeriodPaletteDao
import app.hued.data.local.dao.ProcessingCheckpointDao
import app.hued.data.local.dao.StreakDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HuedDatabase =
        Room.databaseBuilder(
            context,
            HuedDatabase::class.java,
            "hued-database",
        )
            .addMigrations(HuedDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun providePaletteResultDao(db: HuedDatabase): PaletteResultDao = db.paletteResultDao()

    @Provides
    fun providePeriodPaletteDao(db: HuedDatabase): PeriodPaletteDao = db.periodPaletteDao()

    @Provides
    fun provideStreakDao(db: HuedDatabase): StreakDao = db.streakDao()

    @Provides
    fun provideDelightStateDao(db: HuedDatabase): DelightStateDao = db.delightStateDao()

    @Provides
    fun provideProcessingCheckpointDao(db: HuedDatabase): ProcessingCheckpointDao = db.processingCheckpointDao()

    @Provides
    fun provideExcludedFolderDao(db: HuedDatabase): ExcludedFolderDao = db.excludedFolderDao()
}
