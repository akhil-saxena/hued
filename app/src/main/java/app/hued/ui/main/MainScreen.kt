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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hued.R
import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.hued.ui.browse.BrowseScreen
import app.hued.ui.components.PaletteStrip
import app.hued.ui.components.PillButton
import app.hued.ui.components.TimePeriodSelector
import app.hued.ui.insights.InsightsScreen
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
    var showInsights by remember { mutableStateOf(false) }
    var showBrowse by remember { mutableStateOf(false) }

    if (state.showSettings) {
        app.hued.ui.settings.SettingsScreen(
            folders = state.folders,
            onToggleFolder = { path, include -> onEvent(MainEvent.ToggleFolder(path, include)) },
            onReprocess = { onEvent(MainEvent.ReprocessGallery) },
            onClose = { onEvent(MainEvent.HideSettings) },
        )
        return
    }
    if (showInsights) {
        InsightsScreen(onClose = { showInsights = false })
        return
    }
    if (showBrowse) {
        BrowseScreen(onClose = { showBrowse = false })
        return
    }

    val periods = TimePeriod.entries
    val currentIndex = periods.indexOf(state.activePeriod)

    val isProcessing = state.processingState is ProcessingState.InitialProcessing
    val isUpdatingHistory = state.processingState is ProcessingState.UpdatingHistory

    // Only take over the whole screen on the very first run, when there's nothing to show yet.
    // Incremental processing of newly-added photos shouldn't blank out existing palettes.
    if (isProcessing && state.currentPalette == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            val processing = state.processingState as ProcessingState.InitialProcessing
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.building_color_history),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LocalHuedTextMuted.current,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (processing.totalFound > 0) {
                        stringResource(R.string.processing_progress, processing.totalProcessed, processing.totalFound)
                    } else {
                        stringResource(R.string.scanning_gallery)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp),
    ) {
        // Sticky wordmark + actions
        Spacer(modifier = Modifier.height(8.dp))
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
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalHuedTextMuted.current,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIcon(
                    iconRes = R.drawable.ic_browse,
                    contentDescription = stringResource(R.string.browse),
                    onClick = { showBrowse = true },
                )
                HeaderIcon(
                    iconRes = R.drawable.ic_insights,
                    contentDescription = stringResource(R.string.insights),
                    onClick = { showInsights = true },
                )
                HeaderIcon(
                    iconRes = R.drawable.ic_settings,
                    contentDescription = stringResource(R.string.settings),
                    onClick = { onEvent(MainEvent.ShowSettings) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

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
                // Eyebrow: only claim "this week/month/year" when the hero palette actually covers the
                // live period. Otherwise the newest data is older, so say "most recent" instead.
                val periodWord = if (current.isCurrentPeriod) {
                    when (state.activePeriod) {
                        TimePeriod.WEEK -> stringResource(R.string.this_week)
                        TimePeriod.MONTH -> stringResource(R.string.this_month)
                        TimePeriod.YEAR -> stringResource(R.string.this_year)
                    }
                } else {
                    stringResource(R.string.most_recent)
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
                            text = stringResource(R.string.images_count, current.photoCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalHuedTextMuted.current.copy(alpha = 0.6f),
                        )
                    }
                }
                if (state.streakWeeks >= 2) {
                    Text(
                        text = stringResource(R.string.streak_weeks, state.streakWeeks),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalHuedTextMuted.current.copy(alpha = 0.7f),
                    )
                }
                if (current.photoCount in 1..4) {
                    Text(
                        text = stringResource(R.string.few_photos_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalHuedTextMuted.current.copy(alpha = 0.4f),
                    )
                }
                if (state.permissionState is PermissionState.Partial) {
                    Text(
                        text = stringResource(R.string.partial_access_note),
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
                // Optional gentle delight observation
                state.delightMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                        color = LocalHuedTextMuted.current.copy(alpha = 0.6f),
                    )
                }
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
                    PillButton(
                        text = stringResource(R.string.share),
                        onClick = { onEvent(MainEvent.SharePalette(current.id)) },
                        color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }

                // Top color by weight — hidden when expanded (expanded shows full list)
                AnimatedVisibility(
                    visible = !heroExpanded,
                    enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(150)) + fadeOut(tween(100)),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        val top = current.colorNames.zip(current.colors)
                            .zip(current.colorWeights.ifEmpty { List(current.colors.size) { 1f } })
                            .map { (nameColor, weight) -> Triple(nameColor.first, nameColor.second, weight) }
                            .maxByOrNull { it.third }
                        if (top != null) {
                            ColorSwatchRow(name = top.first, color = top.second)
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
                        val colorLimit = if (state.showAllColorNames) current.colorNames.size else 5
                        current.colorNames.zip(current.colors).take(colorLimit).forEach { (name, color) ->
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
                        state.processingState is ProcessingState.Updating ->
                            stringResource(R.string.refreshing_palette)
                        state.permissionState is PermissionState.Denied ||
                            state.permissionState is PermissionState.Revoked ->
                            stringResource(R.string.permission_needed)
                        state.permissionState is PermissionState.Partial ->
                            stringResource(R.string.no_photos_partial)
                        else -> stringResource(R.string.no_photos_message)
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

        // On this day — palette from this week, a year ago
        val onThisDay = state.onThisDay
        if (onThisDay != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.on_this_day),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                PaletteStrip(
                    colors = onThisDay.colors,
                    height = 28.dp,
                    colorNames = onThisDay.colorNames,
                    cornerRadius = 3.dp,
                )
                if (onThisDay.poeticDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = onThisDay.poeticDescription,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = LocalHuedTextMuted.current,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // History — always visible, extra spacing to keep hero as main focus
        if (state.history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.earlier),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = palette.periodLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalHuedTextMuted.current,
                        )
                        if (palette.photoCount in 1..4) {
                            Text(
                                text = stringResource(R.string.few_photos),
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalHuedTextMuted.current.copy(alpha = 0.35f),
                            )
                        }
                    }
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
                                PillButton(
                                    text = stringResource(R.string.share),
                                    onClick = { onEvent(MainEvent.SharePalette(palette.id)) },
                                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val historyColorLimit = if (state.showAllColorNames) palette.colorNames.size else 5
                            palette.colorNames.zip(palette.colors).take(historyColorLimit).forEach { (name, color) ->
                                ColorSwatchRow(name = name, color = color)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // Processing older data footer
        if (isUpdatingHistory) {
            item {
                val updating = state.processingState as ProcessingState.UpdatingHistory
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.processing_earlier),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalHuedTextMuted.current.copy(alpha = 0.4f),
                    )
                    Text(
                        text = stringResource(R.string.count_of_total, updating.totalProcessed, updating.totalFound),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalHuedTextMuted.current.copy(alpha = 0.3f),
                    )
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
private fun HeaderIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = LocalHuedTextMuted.current.copy(alpha = 0.55f),
        )
    }
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
