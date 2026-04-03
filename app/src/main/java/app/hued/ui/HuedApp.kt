package app.hued.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.hued.data.model.PermissionState
import app.hued.ui.main.MainEvent
import app.hued.ui.main.MainScreen
import app.hued.ui.main.MainViewModel
import app.hued.ui.onboarding.OnboardingScreen
import app.hued.ui.share.ShareCardRenderer
import app.hued.ui.theme.HuedTheme

@Composable
fun HuedApp() {
    val viewModel: MainViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val paletteColors = state.currentPalette?.colors ?: emptyList()

    // Handle share target
    LaunchedEffect(state.shareTarget) {
        state.shareTarget?.let { palette ->
            ShareCardRenderer.renderAndShare(context, palette)
            viewModel.clearShareTarget()
        }
    }

    HuedTheme(paletteColors = paletteColors) {
        if (!state.hasCompletedOnboarding && state.permissionState is PermissionState.NotRequested) {
            OnboardingScreen(
                onPermissionResult = { granted ->
                    if (granted) {
                        viewModel.onEvent(MainEvent.PermissionGranted)
                    } else {
                        viewModel.onEvent(MainEvent.PermissionDenied)
                    }
                },
                onComplete = {
                    viewModel.onEvent(MainEvent.OnboardingComplete)
                },
            )
        } else {
            MainScreen(viewModel = viewModel)
        }
    }
}
