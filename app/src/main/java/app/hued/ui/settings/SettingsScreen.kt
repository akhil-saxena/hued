package app.hued.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hued.R
import app.hued.data.DevToolsSettings
import app.hued.data.DevToolsSettingsProvider
import app.hued.ui.components.PillButton
import app.hued.ui.folders.FolderUiState
import app.hued.ui.theme.LocalHuedTextMuted
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    fun devToolsSettingsProvider(): DevToolsSettingsProvider
}

@Composable
fun SettingsScreen(
    folders: List<FolderUiState>,
    onToggleFolder: (path: String, include: Boolean) -> Unit,
    onReprocess: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java)
    }
    val settingsProvider = remember { entryPoint.devToolsSettingsProvider() }
    val settings by settingsProvider.settingsFlow.collectAsState(initial = DevToolsSettings())
    var showDoneDialog by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    // Done confirmation dialog — only if changes were made
    if (showDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDoneDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.apply_changes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            text = {
                Text(
                    stringResource(R.string.reprocess_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalHuedTextMuted.current,
                )
            },
            confirmButton = {
                PillButton(
                    text = stringResource(R.string.reprocess_all),
                    onClick = { showDoneDialog = false; onReprocess() },
                )
            },
            dismissButton = {
                PillButton(
                    text = stringResource(R.string.discard),
                    onClick = { showDoneDialog = false; onClose() },
                    color = LocalHuedTextMuted.current,
                )
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        // Header
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            PillButton(
                text = stringResource(R.string.done),
                onClick = {
                    if (hasChanges) showDoneDialog = true
                    else onClose()
                },
                color = LocalHuedTextMuted.current,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── OPTIONS ──
        SectionLabel(stringResource(R.string.options))
        Spacer(modifier = Modifier.height(10.dp))

        // Palette depth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.palette_depth), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.palette_depth_value, settings.paletteDepth), style = MaterialTheme.typography.labelSmall, color = LocalHuedTextMuted.current.copy(alpha = 0.5f))
        }
        Slider(
            value = settings.paletteDepth.toFloat(),
            onValueChange = { scope.launch { settingsProvider.setPaletteDepth(it.toInt()); hasChanges = true } },
            valueRange = 3f..15f,
            steps = 11,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
            ),
        )

        CompactToggle(stringResource(R.string.weighted_bands), settings.weightedBands) { scope.launch { settingsProvider.setWeightedBands(!settings.weightedBands); hasChanges = true } }

        CompactToggle(stringResource(R.string.match_color_list), settings.showAllColorNames) { scope.launch { settingsProvider.setShowAllColorNames(!settings.showAllColorNames) } }

        Spacer(modifier = Modifier.height(16.dp))
        SectionDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // ── PHOTO SOURCES ──
        SectionLabel(stringResource(R.string.photo_sources))
        Spacer(modifier = Modifier.height(8.dp))

        folders.forEach { folder ->
            CompactFolderRow(
                folder = folder,
                onToggle = { onToggleFolder(folder.path, it); hasChanges = true },
            )
        }

        // Footer
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.version, app.hued.BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = LocalHuedTextMuted.current.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = LocalHuedTextMuted.current)
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(thickness = 0.5.dp, color = LocalHuedTextMuted.current.copy(alpha = 0.1f))
}

@Composable
private fun CompactToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch) { onToggle() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        Switch(
            checked = checked,
            // Row owns the toggle semantics/click; null here prevents a double-fire.
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                uncheckedThumbColor = LocalHuedTextMuted.current.copy(alpha = 0.4f),
            ),
        )
    }
}

@Composable
private fun CompactFolderRow(folder: FolderUiState, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = folder.isIncluded, role = Role.Checkbox) { onToggle(it) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (folder.isIncluded) "\u2713" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.width(16.dp),
            )
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (folder.isIncluded) MaterialTheme.colorScheme.onBackground
                    else LocalHuedTextMuted.current.copy(alpha = 0.35f),
            )
        }
        Text(
            text = "${folder.photoCount}",
            style = MaterialTheme.typography.bodySmall,
            color = LocalHuedTextMuted.current.copy(alpha = if (folder.isIncluded) 0.5f else 0.2f),
        )
    }
}
