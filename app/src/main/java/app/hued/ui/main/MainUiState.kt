package app.hued.ui.main

import app.hued.data.model.DelightMoment
import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import androidx.compose.ui.graphics.Color

data class PeriodPaletteUi(
    val id: Long,
    val periodLabel: String,
    val colors: List<Color>,
    val colorNames: List<String>,
    val poeticDescription: String,
    val photoCount: Int,
    val dominantColorName: String?,
    val streakText: String? = null,
    val favoriteColor: String? = null,
)

data class MainUiState(
    val currentPalette: PeriodPaletteUi? = null,
    val history: List<PeriodPaletteUi> = emptyList(),
    val activePeriod: TimePeriod = TimePeriod.MONTH,
    val expandedPeriodId: Long? = null,
    val delightMoment: DelightMoment? = null,
    val processingState: ProcessingState = ProcessingState.Ready,
    val permissionState: PermissionState = PermissionState.NotRequested,
    val hasCompletedOnboarding: Boolean = false,
    val favoriteColorName: String? = null,
    val shareTarget: PeriodPaletteUi? = null,
    val activeStreakText: String? = null,
)

sealed interface MainEvent {
    data class SelectPeriod(val period: TimePeriod) : MainEvent
    data class ToggleExpand(val paletteId: Long) : MainEvent
    data class SharePalette(val paletteId: Long) : MainEvent
    data object PermissionGranted : MainEvent
    data object PermissionDenied : MainEvent
    data object OnboardingComplete : MainEvent
    data object RetryPermission : MainEvent
}
