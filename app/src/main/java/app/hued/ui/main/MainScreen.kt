package app.hued.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hued.BuildConfig
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.hued.ui.components.ColorNameRow
import app.hued.ui.components.MonthHistoryRow
import app.hued.ui.components.PaletteStrip
import app.hued.ui.components.PoeticDescription
import app.hued.ui.components.TimePeriodSelector
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun MainScreenContent(
    state: MainUiState,
    onEvent: (MainEvent) -> Unit,
) {
    var showDevTools by remember { mutableStateOf(false) }
    var wordmarkTapCount by remember { mutableIntStateOf(0) }

    if (showDevTools && BuildConfig.SHOW_DEV_TOOLS) {
        app.hued.ui.devtools.DevToolsScreen()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        // Wordmark + tagline (triple-tap opens dev tools in debug)
        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "hued",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable {
                    wordmarkTapCount++
                    if (wordmarkTapCount >= 5 && BuildConfig.SHOW_DEV_TOOLS) {
                        showDevTools = true
                    }
                },
            )
            Text(
                text = "your life in color",
                style = MaterialTheme.typography.bodySmall,
                color = LocalHuedTextMuted.current,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimePeriodSelector(
                selected = state.activePeriod,
                onSelect = { onEvent(MainEvent.SelectPeriod(it)) },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Current palette hero (capped to top 5 for clean display)
        val current = state.currentPalette
        if (current != null) {
            val heroColors = current.colors.take(5)
            val heroNames = current.colorNames.take(5)
            item {
                PaletteStrip(
                    colors = heroColors,
                    height = 80.dp,
                    colorNames = heroNames,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = current.periodLabel,
                    style = MaterialTheme.typography.displaySmall,
                )
                if (current.dominantColorName != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = current.dominantColorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalHuedTextMuted.current,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                PoeticDescription(text = current.poeticDescription)
                state.activeStreakText?.let { streak ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = streak,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalHuedTextMuted.current,
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            // No palette yet — show empty state
            item {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Take some photos and come back — your colors are waiting.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalHuedTextMuted.current,
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // History
        items(
            items = state.history,
            key = { it.id },
        ) { palette ->
            MonthHistoryRow(
                monthLabel = palette.periodLabel,
                colors = palette.colors.take(5),
                colorNames = palette.colorNames.take(5),
                poeticDescription = palette.poeticDescription,
                isExpanded = state.expandedPeriodId == palette.id,
                onClick = { onEvent(MainEvent.ToggleExpand(palette.id)) },
                streakText = palette.streakText,
                favoriteColor = palette.favoriteColor,
                onShareClick = { onEvent(MainEvent.SharePalette(palette.id)) },
            )
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
