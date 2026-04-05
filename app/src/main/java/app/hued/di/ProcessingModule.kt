package app.hued.di

import android.content.Context
import app.hued.data.DevToolsSettingsProvider
import app.hued.processing.ColorAggregator
import app.hued.processing.ColorNamer
import app.hued.processing.GalleryScanner
import app.hued.processing.PaletteExtractor
import app.hued.processing.PoeticDescriptionMatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProcessingModule {

    @Provides
    @Singleton
    fun provideDevToolsSettingsProvider(@ApplicationContext context: Context): DevToolsSettingsProvider =
        DevToolsSettingsProvider(context)

    @Provides
    @Singleton
    fun provideGalleryScanner(@ApplicationContext context: Context): GalleryScanner =
        GalleryScanner(context)

    @Provides
    @Singleton
    fun providePaletteExtractor(@ApplicationContext context: Context): PaletteExtractor =
        PaletteExtractor(context)

    @Provides
    @Singleton
    fun provideColorNamer(@ApplicationContext context: Context): ColorNamer =
        ColorNamer(context)

    @Provides
    @Singleton
    fun providePoeticDescriptionMatcher(
        @ApplicationContext context: Context,
    ): PoeticDescriptionMatcher = PoeticDescriptionMatcher(context)

    @Provides
    @Singleton
    fun provideColorAggregator(
        colorNamer: ColorNamer,
        poeticDescriptionMatcher: PoeticDescriptionMatcher,
    ): ColorAggregator = ColorAggregator(colorNamer, poeticDescriptionMatcher)
}
