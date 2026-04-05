package app.hued.ui.main

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hued.data.DevToolsSettingsProvider
import app.hued.data.model.PermissionState
import app.hued.data.model.ProcessingState
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.processing.ColorNamer
import app.hued.processing.PermissionStateManager
import app.hued.processing.ProcessingService
import app.hued.util.DateUtils
import app.hued.util.toComposeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
            val label = when (local.activePeriod) {
                TimePeriod.WEEK -> DateUtils.formatWeek(date)
                TimePeriod.MONTH -> DateUtils.formatMonth(date)
                TimePeriod.YEAR -> "${date.year}"
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
            )
        }

        val processingState = when {
            checkpoint != null && !checkpoint.isComplete ->
                ProcessingState.InitialProcessing(checkpoint.totalFound, checkpoint.totalProcessed)
            checkpoint != null && checkpoint.isComplete -> ProcessingState.Ready
            else -> local.processingState
        }

        local.copy(
            permissionState = permState,
            processingState = processingState,
            useWeightedBands = devSettings.weightedBands,
            currentPalette = paletteUiList.firstOrNull(),
            history = paletteUiList.drop(1),
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    init {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            val onboarded = prefs[ONBOARDING_COMPLETE] ?: false
            _localState.update { it.copy(hasCompletedOnboarding = onboarded, isInitialized = true) }
        }
        // Load favorite color + detect patterns
        viewModelScope.launch {
            val favHex = paletteRepository.getFavoriteColor()
            if (favHex != null) {
                val favName = colorNamer.getName(favHex)
                _localState.update { it.copy(favoriteColorName = favName) }
            }
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
                        paletteRepository.removeExcludedFolder(event.path)
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
        }
    }

    private suspend fun loadFolders() {
        val folderCounts = paletteRepository.getFolderCounts()
        val excludedPaths = paletteRepository.getExcludedFolders()
        val folderStates = folderCounts.map { fc ->
            val displayName = fc.folderPath.substringAfterLast("/").ifEmpty { fc.folderPath }
            app.hued.ui.folders.FolderUiState(
                path = fc.folderPath,
                displayName = displayName,
                photoCount = fc.photoCount,
                isIncluded = fc.folderPath !in excludedPaths,
            )
        }
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
}
