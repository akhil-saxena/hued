package app.hued.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.hued.R
import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.hued.ui.components.PaletteStrip
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
    var heroExpanded by remember { mutableStateOf(true) }

    if (state.showSettings) {
        app.hued.ui.settings.SettingsScreen(
            folders = state.folders,
            onToggleFolder = { path, include -> onEvent(MainEvent.ToggleFolder(path, include)) },
            onReprocess = { onEvent(MainEvent.ReprocessGallery) },
            onClose = { onEvent(MainEvent.HideSettings) },
        )
        return
    }

    val periods = TimePeriod.entries
    val currentIndex = periods.indexOf(state.activePeriod)

    val isProcessing = state.processingState is ProcessingState.InitialProcessing

    if (isProcessing) {
        // Full-screen centered processing state — no tabs, no palettes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            val processing = state.processingState as ProcessingState.InitialProcessing
            Text(
                text = if (processing.totalFound > 0) {
                    "${processing.totalProcessed} of ${processing.totalFound} images"
                } else {
                    "Scanning your gallery\u2026"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = LocalHuedTextMuted.current,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        // Sticky wordmark + settings icon
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "hued",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "your life in color",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalHuedTextMuted.current,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = "Settings",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onEvent(MainEvent.ShowSettings) },
                tint = LocalHuedTextMuted.current.copy(alpha = 0.35f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .pointerInput(currentIndex) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        val threshold = 80f
                        if (totalDrag < -threshold && currentIndex < periods.lastIndex) {
                            onEvent(MainEvent.SelectPeriod(periods[currentIndex + 1]))
                        } else if (totalDrag > threshold && currentIndex > 0) {
                            onEvent(MainEvent.SelectPeriod(periods[currentIndex - 1]))
                        }
                    },
                )
            },
    ) {
        item {
            TimePeriodSelector(
                selected = state.activePeriod,
                onSelect = { onEvent(MainEvent.SelectPeriod(it)) },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        val current = state.currentPalette

        if (current != null) {
            // Hero card — minimal
            item {
                // "this week" + image count
                val periodWord = when (state.activePeriod) {
                    TimePeriod.WEEK -> "this week"
                    TimePeriod.MONTH -> "this month"
                    TimePeriod.YEAR -> "this year"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = periodWord,
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalHuedTextMuted.current,
                    )
                    if (current.photoCount > 0) {
                        Text(
                            text = "${current.photoCount} images",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalHuedTextMuted.current.copy(alpha = 0.6f),
                        )
                    }
                }
                if (state.permissionState is PermissionState.Partial) {
                    Text(
                        text = "results reflect selected photos only",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalHuedTextMuted.current.copy(alpha = 0.4f),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Palette strip — tap to expand
                PaletteStrip(
                    colors = current.colors,
                    height = 64.dp,
                    colorNames = current.colorNames,
                    colorWeights = current.colorWeights,
                    useWeightedBands = state.useWeightedBands,
                    modifier = Modifier.clickable { heroExpanded = !heroExpanded },
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Period label + poetic description
                Text(
                    text = current.periodLabel,
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = current.poeticDescription,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = LocalHuedTextMuted.current,
                        modifier = Modifier.weight(1f),
                    )
                    app.hued.ui.components.PillButton(
                        text = "share",
                        onClick = { onEvent(MainEvent.SharePalette(current.id)) },
                        color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }

                // Top 3 colors by weight — hidden when expanded (expanded shows full list)
                AnimatedVisibility(
                    visible = !heroExpanded,
                    enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(150)) + fadeOut(tween(100)),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        run {
                            val indexed = current.colorNames.zip(current.colors)
                                .zip(current.colorWeights.ifEmpty { List(current.colors.size) { 1f } })
                                .map { (nameColor, weight) -> Triple(nameColor.first, nameColor.second, weight) }
                                .sortedByDescending { it.third }
                                .take(3)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                indexed.forEach { (name, color, _) ->
                                    ColorSwatchRow(name = name, color = color)
                                }
                            }
                        }
                    }
                }

                // Expandable full color list
                AnimatedVisibility(
                    visible = heroExpanded,
                    enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit = shrinkVertically(tween(250)) + fadeOut(tween(200)),
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        current.colorNames.zip(current.colors).take(5).forEach { (name, color) ->
                            ColorSwatchRow(name = name, color = color)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = when {
                        state.processingState is ProcessingState.Updating -> "Refreshing your palette\u2026"
                        state.permissionState is PermissionState.Denied ||
                            state.permissionState is PermissionState.Revoked ->
                            "Gallery access needed to discover your colors."
                        state.permissionState is PermissionState.Partial ->
                            "No colors for this period \u2014 try granting access to more photos."
                        else -> "No colors for this period yet \u2014 keep taking photos."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalHuedTextMuted.current,
                )
                if (state.processingState is ProcessingState.Updating) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // History — always visible, extra spacing to keep hero as main focus
        if (state.history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "earlier",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(
                items = state.history,
                key = { it.id },
            ) { palette ->
                val isExpanded = state.expandedPeriodId == palette.id

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(MainEvent.ToggleExpand(palette.id)) }
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        text = palette.periodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalHuedTextMuted.current,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PaletteStrip(
                        colors = palette.colors,
                        height = if (isExpanded) 48.dp else 28.dp,
                        colorNames = palette.colorNames,
                        cornerRadius = 3.dp,
                    )

                    // Expanded: poetic description + color list
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300, delayMillis = 100)),
                        exit = shrinkVertically(tween(250)) + fadeOut(tween(200)),
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = palette.poeticDescription,
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color = LocalHuedTextMuted.current,
                                    modifier = Modifier.weight(1f),
                                )
                                app.hued.ui.components.PillButton(
                                    text = "share",
                                    onClick = { onEvent(MainEvent.SharePalette(palette.id)) },
                                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            palette.colorNames.zip(palette.colors).take(5).forEach { (name, color) ->
                                ColorSwatchRow(name = name, color = color)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
    } // Column
}

@Composable
private fun ColorSwatchRow(
    name: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = LocalHuedTextMuted.current.copy(alpha = 0.8f),
        )
    }
}
