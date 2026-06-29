package app.hued.ui.main

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hued.R
import app.hued.data.DevToolsSettingsProvider
import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.processing.ColorNamer
import app.hued.processing.GalleryScanner
import app.hued.processing.PermissionStateManager
import app.hued.processing.ProcessingService
import app.hued.util.DateUtils
import app.hued.util.toComposeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "hued_prefs")
private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val paletteRepository: PaletteRepository,
    private val permissionStateManager: PermissionStateManager,
    private val galleryScanner: GalleryScanner,
    private val colorNamer: ColorNamer,
    private val devToolsSettingsProvider: DevToolsSettingsProvider,
    private val notificationScheduler: app.hued.notification.NotificationScheduler,
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private val _localState = MutableStateFlow(MainUiState())

    private val palettesFlow = _localState
        .map { it.activePeriod }
        .flatMapLatest { period -> paletteRepository.getAllPalettes(period) }

    private val checkpointFlow = paletteRepository.observeCheckpoint()
    private val devToolsFlow = devToolsSettingsProvider.settingsFlow

    val uiState: StateFlow<MainUiState> = combine(
        _localState,
        permissionStateManager.state,
        palettesFlow,
        checkpointFlow,
        devToolsFlow,
    ) { local, permState, palettes, checkpoint, devSettings ->
        val paletteUiList = palettes.map { entity ->
            val hexColors = json.decodeFromString<List<String>>(entity.colors)
            val composeColors = hexColors.map { it.toComposeColor() }
            val names = hexColors.map { colorNamer.getName(it) }
            val date = LocalDate.ofEpochDay(entity.startDate)
            val today = LocalDate.now()
            val label = when (local.activePeriod) {
                TimePeriod.WEEK -> DateUtils.formatWeek(date)
                TimePeriod.MONTH -> DateUtils.formatMonth(date)
                TimePeriod.YEAR -> "${date.year}"
            }
            val isCurrentPeriod = when (local.activePeriod) {
                TimePeriod.WEEK -> entity.startDate == DateUtils.startOfWeek(today).toEpochDay()
                TimePeriod.MONTH -> entity.startDate == DateUtils.startOfMonth(today).toEpochDay()
                TimePeriod.YEAR -> entity.startDate == DateUtils.startOfYear(today).toEpochDay()
            }
            val weights = entity.colorWeights?.let {
                try {
                    json.decodeFromString<List<Float>>(it)
                } catch (e: Exception) {
                    android.util.Log.w("MainViewModel", "Failed to decode colorWeights for palette ${entity.id}", e)
                    emptyList()
                }
            } ?: emptyList()

            PeriodPaletteUi(
                id = entity.id,
                periodLabel = label,
                colors = composeColors,
                colorNames = names,
                colorWeights = weights,
                poeticDescription = entity.poeticDescription,
                photoCount = entity.photoCount,
                dominantColorName = colorNamer.getName(entity.dominantColor),
                favoriteColor = colorNamer.getName(entity.dominantColor),
                isCurrentPeriod = isCurrentPeriod,
            )
        }

        val processingState = when {
            checkpoint != null && !checkpoint.isComplete && !checkpoint.currentYearDone ->
                ProcessingState.InitialProcessing(checkpoint.totalFound, checkpoint.totalProcessed)
            checkpoint != null && !checkpoint.isComplete && checkpoint.currentYearDone ->
                ProcessingState.UpdatingHistory(checkpoint.totalFound, checkpoint.totalProcessed)
            checkpoint != null && checkpoint.isComplete -> ProcessingState.Ready
            else -> local.processingState
        }

        val current = paletteUiList.firstOrNull()
        val currentEntity = palettes.firstOrNull()
        val delight = if (current != null && currentEntity != null) {
            detectDelight(current.colors, LocalDate.ofEpochDay(currentEntity.startDate), local.activePeriod)
        } else {
            null
        }

        local.copy(
            permissionState = permState,
            processingState = processingState,
            useWeightedBands = devSettings.weightedBands,
            showAllColorNames = devSettings.showAllColorNames,
            currentPalette = current,
            history = paletteUiList.drop(1),
            delightMessage = delight,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    init {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            val onboarded = prefs[ONBOARDING_COMPLETE] ?: false
            _localState.update { it.copy(hasCompletedOnboarding = onboarded, isInitialized = true) }

            // After init, check for new folders & images if already onboarded with permissions
            if (onboarded && permissionStateManager.state.value != PermissionState.NotRequested) {
                checkForUpdates()
            }
        }
        // Load favorite color + detect patterns
        viewModelScope.launch {
            val favHex = paletteRepository.getFavoriteColor()
            if (favHex != null) {
                val favName = colorNamer.getName(favHex)
                _localState.update { it.copy(favoriteColorName = favName) }
            }
        }
        // Recompute streak + on-this-day whenever a processing run completes (or on open).
        viewModelScope.launch {
            paletteRepository.observeCheckpoint()
                .map { it?.isComplete == true }
                .distinctUntilChanged()
                .collect { recomputeExtras() }
        }
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.SelectPeriod -> {
                _localState.update { it.copy(activePeriod = event.period, expandedPeriodId = null) }
            }
            is MainEvent.ToggleExpand -> {
                _localState.update { state ->
                    val newExpanded = if (state.expandedPeriodId == event.paletteId) null else event.paletteId
                    state.copy(expandedPeriodId = newExpanded)
                }
            }
            is MainEvent.SharePalette -> {
                sharePalette(event.paletteId)
            }
            is MainEvent.PermissionGranted -> {
                permissionStateManager.refresh()
                startProcessing()
            }
            is MainEvent.PermissionDenied -> {
                permissionStateManager.updateState(PermissionState.Denied)
            }
            is MainEvent.OnboardingComplete -> {
                viewModelScope.launch {
                    context.dataStore.edit { it[ONBOARDING_COMPLETE] = true }
                    _localState.update { it.copy(hasCompletedOnboarding = true) }
                }
            }
            is MainEvent.RetryPermission -> {
                // Handled by composable re-launching permission request
            }
            is MainEvent.ShowSettings -> {
                viewModelScope.launch {
                    loadFolders()
                    _localState.update { it.copy(showSettings = true) }
                }
            }
            is MainEvent.HideSettings -> {
                _localState.update { it.copy(showSettings = false) }
            }
            is MainEvent.ToggleFolder -> {
                viewModelScope.launch {
                    if (event.include) {
                        // Remove both the full path AND any default exclusion that matches
                        paletteRepository.removeExcludedFolder(event.path)
                        val excludedPaths = paletteRepository.getExcludedFolders()
                        excludedPaths.filter { event.path.contains(it, ignoreCase = true) }
                            .forEach { paletteRepository.removeExcludedFolder(it) }
                    } else {
                        paletteRepository.addExcludedFolder(event.path)
                    }
                    loadFolders()
                }
            }
            is MainEvent.ReprocessGallery -> {
                viewModelScope.launch {
                    // Always full reprocess: clear everything so settings take effect
                    paletteRepository.clearCheckpoint()
                    paletteRepository.deleteAllPalettes()
                    paletteRepository.deleteAllResults()
                    _localState.update {
                        it.copy(
                            showSettings = false,
                            processingState = ProcessingState.InitialProcessing(0, 0),
                        )
                    }
                    val intent = Intent(context, ProcessingService::class.java)
                    context.startForegroundService(intent)
                }
            }
            is MainEvent.ToggleNewFolder -> {
                _localState.update { state ->
                    val updated = state.newFolders.map { folder ->
                        if (folder.path == event.path) folder.copy(isIncluded = event.include)
                        else folder
                    }
                    state.copy(newFolders = updated)
                }
            }
            is MainEvent.ConfirmNewFolders -> {
                viewModelScope.launch {
                    val state = _localState.value
                    // Exclude folders the user unchecked
                    state.newFolders.filter { !it.isIncluded }.forEach { folder ->
                        paletteRepository.addExcludedFolder(folder.path)
                    }
                    _localState.update { it.copy(showNewFoldersDialog = false, newFolders = emptyList()) }
                    // Kick off incremental processing for newly included images
                    startIncrementalProcessing()
                }
            }
            is MainEvent.DismissNewFolders -> {
                viewModelScope.launch {
                    // Exclude all new folders if user dismisses
                    val state = _localState.value
                    state.newFolders.forEach { folder ->
                        paletteRepository.addExcludedFolder(folder.path)
                    }
                    _localState.update { it.copy(showNewFoldersDialog = false, newFolders = emptyList()) }
                }
            }
        }
    }

    private suspend fun loadFolders() {
        val excludedPaths = paletteRepository.getExcludedFolders()
        val deviceFolders = withContext(Dispatchers.IO) {
            galleryScanner.discoverAllFolders()
        }

        // Only show folders that currently exist on the device
        val deviceMap = deviceFolders.associate { it.path to it.imageCount }

        val folderStates = deviceMap.keys.map { path ->
            val displayName = path.substringAfterLast("/").ifEmpty { path }
            app.hued.ui.folders.FolderUiState(
                path = path,
                displayName = displayName,
                photoCount = deviceMap[path] ?: 0,
                isIncluded = excludedPaths.none { path.contains(it, ignoreCase = true) },
            )
        }.sortedByDescending { it.photoCount }

        _localState.update { it.copy(folders = folderStates) }
    }

    private fun startProcessing() {
        _localState.update { it.copy(processingState = ProcessingState.InitialProcessing(0, 0)) }
        val intent = Intent(context, ProcessingService::class.java)
        context.startForegroundService(intent)
        // Schedule weekly Monday notifications
        notificationScheduler.scheduleWeeklyRefresh()
    }

    private fun sharePalette(paletteId: Long) {
        val state = uiState.value
        val palette = if (state.currentPalette?.id == paletteId) state.currentPalette
            else state.history.find { it.id == paletteId }
            ?: return

        viewModelScope.launch {
            _localState.update {
                it.copy(shareTarget = palette, shareTargetPeriod = state.activePeriod)
            }
        }
    }

    fun clearShareTarget() {
        _localState.update { it.copy(shareTarget = null, shareTargetPeriod = null) }
    }

    private suspend fun checkForUpdates() {
        val allDeviceFolders = withContext(Dispatchers.IO) {
            galleryScanner.discoverAllFolders()
        }
        val excludedPaths = withContext(Dispatchers.IO) {
            paletteRepository.getExcludedFolders()
        }
        val knownFolders = withContext(Dispatchers.IO) {
            paletteRepository.getFolderCounts().map { it.folderPath }.toSet()
        }

        // Folders on device that we haven't seen and aren't already excluded
        val newFolders = allDeviceFolders.filter { folder ->
            folder.path !in knownFolders &&
                excludedPaths.none { folder.path.contains(it, ignoreCase = true) }
        }

        if (newFolders.isNotEmpty()) {
            val folderStates = newFolders.map { folder ->
                app.hued.ui.folders.FolderUiState(
                    path = folder.path,
                    displayName = folder.path.substringAfterLast("/").ifEmpty { folder.path },
                    photoCount = folder.imageCount,
                    isIncluded = true,
                )
            }
            _localState.update {
                it.copy(newFolders = folderStates, showNewFoldersDialog = true)
            }
        } else {
            // No new folders — check if there are actually new images before starting service
            val checkpoint = withContext(Dispatchers.IO) { paletteRepository.getCheckpoint() }
            val sinceTimestamp = checkpoint?.lastTimestamp ?: 0L
            val excludedFolders = withContext(Dispatchers.IO) { paletteRepository.getExcludedFolders() }
            val newImages = withContext(Dispatchers.IO) {
                galleryScanner.scanGallery(excludedFolders, sinceTimestamp)
            }
            if (newImages.isNotEmpty()) {
                startIncrementalProcessing()
            }
        }
    }

    private fun startIncrementalProcessing() {
        val intent = Intent(context, ProcessingService::class.java)
        context.startForegroundService(intent)
    }

    /** Recompute the streak count and the "a year ago this week" memory. */
    private suspend fun recomputeExtras() {
        val today = LocalDate.now()
        val weekStarts = withContext(Dispatchers.IO) { paletteRepository.getWeekStartDates() }.toHashSet()
        val streak = computeStreak(weekStarts, today)

        val lastYearStart = DateUtils.startOfWeek(today).minusWeeks(52).toEpochDay()
        val otd = withContext(Dispatchers.IO) {
            paletteRepository.getPaletteByStart(TimePeriod.WEEK, lastYearStart)
        }
        val otdUi = otd?.let { entity ->
            val hex = try {
                json.decodeFromString<List<String>>(entity.colors)
            } catch (e: Exception) {
                emptyList()
            }
            PeriodPaletteUi(
                id = entity.id,
                periodLabel = DateUtils.formatWeek(LocalDate.ofEpochDay(entity.startDate)),
                colors = hex.map { it.toComposeColor() },
                colorNames = hex.map { colorNamer.getName(it) },
                poeticDescription = entity.poeticDescription,
                photoCount = entity.photoCount,
                dominantColorName = colorNamer.getName(entity.dominantColor),
            )
        }

        _localState.update { it.copy(streakWeeks = streak, onThisDay = otdUi) }
    }

    /** Consecutive ISO weeks (ending at the current week, with a one-week grace) that captured color. */
    private fun computeStreak(weekStartDays: Set<Long>, today: LocalDate): Int {
        if (weekStartDays.isEmpty()) return 0
        var cursor = DateUtils.startOfWeek(today)
        // Grace: the current week may not have photos yet — start from last week if so.
        if (cursor.toEpochDay() !in weekStartDays) {
            cursor = cursor.minusWeeks(1)
        }
        var streak = 0
        while (cursor.toEpochDay() in weekStartDays) {
            streak++
            cursor = cursor.minusWeeks(1)
        }
        return streak
    }

    /** A gentle, optional observation about the current palette. Returns null when nothing stands out. */
    private fun detectDelight(colors: List<Color>, startDate: LocalDate, period: TimePeriod): String? {
        if (period == TimePeriod.WEEK && startDate.monthValue == 1 && startDate.dayOfMonth <= 7) {
            return context.getString(R.string.delight_new_year)
        }
        if (colors.size < 3) return null
        val word = periodWord(period)
        return try {
            val hsls = colors.map { c ->
                FloatArray(3).also { ColorUtils.colorToHSL(c.toArgb(), it) }
            }
            val saturated = hsls.filter { it[1] >= 0.15f && it[2] in 0.12f..0.90f }
            when {
                saturated.size <= 1 -> context.getString(R.string.delight_neutral, word)
                saturated.size >= 3 -> {
                    val hues = saturated.map { it[0] }.sorted()
                    var maxGap = 360f - (hues.last() - hues.first())
                    for (i in 1 until hues.size) {
                        maxGap = maxOf(maxGap, hues[i] - hues[i - 1])
                    }
                    val arc = 360f - maxGap // angular span the hues occupy
                    when {
                        arc <= 45f -> context.getString(R.string.delight_monochrome, word)
                        arc >= 300f -> context.getString(R.string.delight_rainbow, word)
                        else -> null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun periodWord(period: TimePeriod): String = when (period) {
        TimePeriod.WEEK -> context.getString(R.string.period_week)
        TimePeriod.MONTH -> context.getString(R.string.period_month)
        TimePeriod.YEAR -> context.getString(R.string.period_year)
    }
}
