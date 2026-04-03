package app.hued.di

import app.hued.data.repository.DelightRepository
import app.hued.data.repository.DelightRepositoryImpl
import app.hued.data.repository.PaletteRepository
import app.hued.data.repository.PaletteRepositoryImpl
import app.hued.data.repository.StreakRepository
import app.hued.data.repository.StreakRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaletteRepository(impl: PaletteRepositoryImpl): PaletteRepository

    @Binds
    @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository

    @Binds
    @Singleton
    abstract fun bindDelightRepository(impl: DelightRepositoryImpl): DelightRepository
}
