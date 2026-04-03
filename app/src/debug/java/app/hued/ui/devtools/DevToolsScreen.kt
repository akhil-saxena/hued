package app.hued.ui.devtools

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.devToolsDataStore by preferencesDataStore(name = "dev_tools")
private val PALETTE_DEPTH = intPreferencesKey("palette_depth")
private val WEIGHTED_BANDS = booleanPreferencesKey("weighted_bands")
private val GRADIENT_WASH = booleanPreferencesKey("gradient_wash")
private val COLOR_BLEED = booleanPreferencesKey("color_bleed")

@Composable
fun DevToolsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs by context.devToolsDataStore.data.collectAsState(initial = null)
    val paletteDepth = prefs?.get(PALETTE_DEPTH) ?: 5
    val weightedBands = prefs?.get(WEIGHTED_BANDS) ?: false
    val gradientWash = prefs?.get(GRADIENT_WASH) ?: false
    val colorBleed = prefs?.get(COLOR_BLEED) ?: false

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Dev Tools",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Palette depth
        Text(
            text = "Palette depth: $paletteDepth colors",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = paletteDepth.toFloat(),
            onValueChange = { newValue ->
                scope.launch {
                    context.devToolsDataStore.edit { it[PALETTE_DEPTH] = newValue.toInt() }
                }
            },
            valueRange = 3f..15f,
            steps = 11,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Weighted bands
        DevToolToggle(
            label = "Weighted color bands",
            checked = weightedBands,
            onToggle = { scope.launch { context.devToolsDataStore.edit { it[WEIGHTED_BANDS] = !weightedBands } } },
        )

        // Gradient wash
        DevToolToggle(
            label = "Gradient wash background",
            checked = gradientWash,
            onToggle = { scope.launch { context.devToolsDataStore.edit { it[GRADIENT_WASH] = !gradientWash } } },
        )

        // Color bleed transitions
        DevToolToggle(
            label = "Color-bleed transitions",
            checked = colorBleed,
            onToggle = { scope.launch { context.devToolsDataStore.edit { it[COLOR_BLEED] = !colorBleed } } },
        )
    }
}

@Composable
private fun DevToolToggle(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
    }
}
