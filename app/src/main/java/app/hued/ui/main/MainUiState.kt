package app.hued.ui.main

import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import app.hued.ui.folders.FolderUiState
import androidx.compose.ui.graphics.Color

data class PeriodPaletteUi(
    val id: Long,
    val periodLabel: String,
    val colors: List<Color>,
    val colorNames: List<String>,
    val colorWeights: List<Float> = emptyList(),
    val poeticDescription: String,
    val photoCount: Int,
    val dominantColorName: String?,
    val favoriteColor: String? = null,
)

data class MainUiState(
    val currentPalette: PeriodPaletteUi? = null,
    val history: List<PeriodPaletteUi> = emptyList(),
    val activePeriod: TimePeriod = TimePeriod.WEEK,
    val useWeightedBands: Boolean = false,
    val expandedPeriodId: Long? = null,
    val processingState: ProcessingState = ProcessingState.Ready,
    val permissionState: PermissionState = PermissionState.NotRequested,
    val isInitialized: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val favoriteColorName: String? = null,
    val shareTarget: PeriodPaletteUi? = null,
    val shareTargetPeriod: TimePeriod? = null,
    val folders: List<FolderUiState> = emptyList(),
    val showSettings: Boolean = false,
)

sealed interface MainEvent {
    data class SelectPeriod(val period: TimePeriod) : MainEvent
    data class ToggleExpand(val paletteId: Long) : MainEvent
    data class SharePalette(val paletteId: Long) : MainEvent
    data object PermissionGranted : MainEvent
    data object PermissionDenied : MainEvent
    data object OnboardingComplete : MainEvent
    data object RetryPermission : MainEvent
    data object ShowSettings : MainEvent
    data object HideSettings : MainEvent
    data class ToggleFolder(val path: String, val include: Boolean) : MainEvent
    data object ReprocessGallery : MainEvent
}
