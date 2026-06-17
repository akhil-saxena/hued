package app.hued.ui.browse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hued.R
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.processing.ColorNamer
import app.hued.ui.components.PaletteStrip
import app.hued.ui.components.PillButton
import app.hued.ui.theme.LocalHuedTextMuted
import app.hued.util.DateUtils
import app.hued.util.toComposeColor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BrowseEntryPoint {
    fun paletteRepository(): PaletteRepository
    fun colorNamer(): ColorNamer
}

private data class BrowseItem(
    val id: Long,
    val label: String,
    val colors: List<Color>,
    val colorNames: List<String>,
)

private val browseJson = Json { ignoreUnknownKeys = true }

@Composable
fun BrowseScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, BrowseEntryPoint::class.java)
    }
    val paletteRepository = remember { entryPoint.paletteRepository() }
    val colorNamer = remember { entryPoint.colorNamer() }

    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(emptyList<BrowseItem>()) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            val palettes = paletteRepository.getAllPalettes(TimePeriod.WEEK).first()
            palettes.map { entity ->
                val hexes = browseJson.decodeFromString<List<String>>(entity.colors)
                BrowseItem(
                    id = entity.id,
                    label = DateUtils.formatWeek(LocalDate.ofEpochDay(entity.startDate)),
                    colors = hexes.map { it.toComposeColor() },
                    colorNames = hexes.map { colorNamer.getName(it) },
                )
            }
        }
        items = loaded
    }

    val filtered = if (query.isBlank()) {
        items
    } else {
        val q = query.trim().lowercase()
        items.filter { item ->
            item.label.lowercase().contains(q) ||
                item.colorNames.any { it.lowercase().contains(q) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp),
    ) {
        // Header
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.browse),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            PillButton(
                text = stringResource(R.string.close),
                onClick = onClose,
                color = LocalHuedTextMuted.current,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalHuedTextMuted.current,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = LocalHuedTextMuted.current.copy(alpha = 0.4f),
                unfocusedBorderColor = LocalHuedTextMuted.current.copy(alpha = 0.2f),
                focusedPlaceholderColor = LocalHuedTextMuted.current,
                unfocusedPlaceholderColor = LocalHuedTextMuted.current,
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalHuedTextMuted.current,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = filtered, key = { it.id }) { item ->
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalHuedTextMuted.current,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PaletteStrip(
                            colors = item.colors,
                            height = 28.dp,
                            cornerRadius = 3.dp,
                            colorNames = item.colorNames,
                        )
                    }
                }
            }
        }
    }
}
