package app.hued.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class DevToolsSettings(
    val paletteDepth: Int = 5,
    val weightedBands: Boolean = false,
)

val Context.devToolsDataStore by preferencesDataStore(name = "dev_tools")
val PALETTE_DEPTH = intPreferencesKey("palette_depth")
val WEIGHTED_BANDS = booleanPreferencesKey("weighted_bands")

@Singleton
class DevToolsSettingsProvider @Inject constructor(
    private val context: Context,
) {
    val settingsFlow: Flow<DevToolsSettings> = context.devToolsDataStore.data.map { prefs ->
        DevToolsSettings(
            paletteDepth = prefs[PALETTE_DEPTH] ?: 5,
            weightedBands = prefs[WEIGHTED_BANDS] ?: false,
        )
    }

    suspend fun getCurrent(): DevToolsSettings = settingsFlow.first()

    suspend fun setPaletteDepth(value: Int) {
        context.devToolsDataStore.edit { it[PALETTE_DEPTH] = value }
    }

    suspend fun setWeightedBands(value: Boolean) {
        context.devToolsDataStore.edit { it[WEIGHTED_BANDS] = value }
    }

}
