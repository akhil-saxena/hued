package app.hued.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.hued.data.model.PermissionState
import app.hued.ui.main.MainEvent
import app.hued.ui.main.MainScreen
import app.hued.ui.main.MainViewModel
import app.hued.ui.onboarding.OnboardingScreen
import app.hued.ui.share.ShareOverlay
import app.hued.ui.theme.HuedCanvasResting
import app.hued.ui.theme.HuedTextPrimary
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun HuedApp(openWeekly: Boolean = false) {
    val viewModel: MainViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle notification intent to open weekly view
    LaunchedEffect(openWeekly) {
        if (openWeekly) {
            viewModel.onEvent(MainEvent.SelectPeriod(app.hued.data.model.TimePeriod.WEEK))
        }
    }

    // Skip onboarding if we already have data (e.g. reinstall with existing gallery)
    val needsOnboarding = !state.hasCompletedOnboarding &&
        state.currentPalette == null &&
        state.processingState is app.hued.data.model.ProcessingState.Ready

    HuedTheme {
        if (!state.isInitialized) {
            SplashScreen()
            return@HuedTheme
        }
        if (needsOnboarding) {
            OnboardingScreen(
                onPermissionResult = { granted ->
                    if (granted) {
                        viewModel.onEvent(MainEvent.PermissionGranted)
                        viewModel.onEvent(MainEvent.OnboardingComplete)
                    }
                    // If denied, stay on onboarding so user can retry
                },
                onComplete = {},
            )
        } else {
            MainScreen(viewModel = viewModel)
        }

        // Share overlay — shown when shareTarget is set
        val shareTarget = state.shareTarget
        val sharePeriod = state.shareTargetPeriod
        if (shareTarget != null && sharePeriod != null) {
            ShareOverlay(
                palette = shareTarget,
                period = sharePeriod,
                isCurrent = state.currentPalette?.id == shareTarget.id,
                onDismiss = { viewModel.clearShareTarget() },
            )
        }
    }
}

private val splashPalette = listOf(
    Color(0xFFCB6D51),
    Color(0xFF3B6B8A),
    Color(0xFF4A7C59),
    Color(0xFF7B6897),
    Color(0xFFC49540),
)

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HuedCanvasResting),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(6.dp)),
            ) {
                splashPalette.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .background(color),
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "your life in color",
                style = MaterialTheme.typography.displaySmall,
                color = HuedTextPrimary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "hued",
                style = MaterialTheme.typography.titleSmall,
                color = LocalHuedTextMuted.current,
            )
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
            )
        }
    }
}
