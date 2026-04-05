package app.hued.ui.devtools

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import app.hued.data.DevToolsSettings
import app.hued.data.DevToolsSettingsProvider
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.repository.PaletteRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.random.Random

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DevToolsEntryPoint {
    fun paletteRepository(): PaletteRepository
    fun devToolsSettingsProvider(): DevToolsSettingsProvider
}

// Color families that shift by season for realistic test data
private val springColors = listOf("#7BC67E", "#A8D5A2", "#F2E68D", "#F7C873", "#E8A87C")
private val summerColors = listOf("#F4A460", "#E8C44D", "#87CEEB", "#4DB8A4", "#FFD700")
private val autumnColors = listOf("#CB6D51", "#D4956A", "#A85A3C", "#8B6914", "#6B4226")
private val winterColors = listOf("#3B6B8A", "#5B93B5", "#85B8D4", "#7B6897", "#9882B0")

private val poeticDescriptions = listOf(
    "Warmth with pockets of shade.",
    "A quiet hum of earth tones.",
    "Cool light through morning glass.",
    "Some days burned, others glowed.",
    "Soft edges, muted afternoons.",
    "The sky kept changing its mind.",
    "Greens leaning into gold.",
    "Still waters, reflected moods.",
    "Amber hours stretched long.",
    "Frost on the edges of color.",
    "Sunlight caught sideways.",
    "Dusk colors lingering past midnight.",
)

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun DevToolsScreen(onClose: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, DevToolsEntryPoint::class.java)
    }
    val settingsProvider = remember { entryPoint.devToolsSettingsProvider() }

    val settings by settingsProvider.settingsFlow.collectAsState(initial = DevToolsSettings())

    var seedStatus by remember { mutableStateOf<String?>(null) }
    var showSharePreview by remember { mutableStateOf(false) }

    if (showSharePreview) {
        app.hued.ui.share.ShareCardPreviewScreen(onClose = { showSharePreview = false })
        return
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Dev Tools",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = "✕",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Palette depth
        Text(
            text = "Palette depth: ${settings.paletteDepth} colors",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = settings.paletteDepth.toFloat(),
            onValueChange = { newValue ->
                scope.launch { settingsProvider.setPaletteDepth(newValue.toInt()) }
            },
            valueRange = 3f..15f,
            steps = 11,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Weighted bands
        DevToolToggle(
            label = "Weighted color bands",
            checked = settings.weightedBands,
            onToggle = { scope.launch { settingsProvider.setWeightedBands(!settings.weightedBands) } },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preview share card layouts
        Button(
            onClick = { showSharePreview = true },
        ) {
            Text("Preview share layouts")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Export share card PNGs
        Button(
            onClick = {
                seedStatus = "Exporting share PNGs…"
                scope.launch {
                    val count = app.hued.ui.share.ShareCardExporter.exportAll(context)
                    seedStatus = "Exported $count PNGs to cache/share_exports/"
                }
            },
        ) {
            Text("Export share PNGs")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Seed test data
        Button(
            onClick = {
                seedStatus = "Seeding…"
                scope.launch {
                    seedTestData(context)
                    onClose()
                }
            },
        ) {
            Text("Seed 2 years of test data")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reprocess gallery — closes dev tools to show progress on main screen
        Button(
            onClick = {
                val intent = android.content.Intent(context, app.hued.processing.ProcessingService::class.java)
                context.startForegroundService(intent)
                onClose()
            },
        ) {
            Text("Reprocess gallery")
        }

        seedStatus?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private suspend fun seedTestData(context: Context) = withContext(Dispatchers.IO) {
    val entryPoint = EntryPointAccessors.fromApplication(context, DevToolsEntryPoint::class.java)
    val repo = entryPoint.paletteRepository()
    val settings = entryPoint.devToolsSettingsProvider().getCurrent()
    val depth = settings.paletteDepth

    // Clear existing palettes
    repo.deleteAllPalettes()

    val now = LocalDate.now()
    val rng = Random(42)

    // Generate 104 weeks (2 years)
    val weekFields = WeekFields.of(Locale.getDefault())
    var weekStart = now.with(weekFields.dayOfWeek(), 1)
    repeat(104) { i ->
        val weekEnd = weekStart.plusDays(7)
        val (colors, weights) = colorsAndWeightsForDate(weekStart, depth, rng)
        repo.savePalette(
            PeriodPaletteEntity(
                periodType = "WEEK",
                startDate = weekStart.toEpochDay(),
                endDate = weekEnd.toEpochDay(),
                colors = json.encodeToString(ListSerializer(String.serializer()), colors),
                photoCount = rng.nextInt(10, 80),
                dominantColor = colors.first(),
                poeticDescription = poeticDescriptions[i % poeticDescriptions.size],
                colorWeights = json.encodeToString(ListSerializer(Float.serializer()), weights),
            )
        )
        weekStart = weekStart.minusWeeks(1)
    }

    // Generate 24 months
    var monthStart = now.withDayOfMonth(1)
    repeat(24) { i ->
        val monthEnd = monthStart.plusMonths(1)
        val (colors, weights) = colorsAndWeightsForDate(monthStart, depth, rng)
        repo.savePalette(
            PeriodPaletteEntity(
                periodType = "MONTH",
                startDate = monthStart.toEpochDay(),
                endDate = monthEnd.toEpochDay(),
                colors = json.encodeToString(ListSerializer(String.serializer()), colors),
                photoCount = rng.nextInt(40, 300),
                dominantColor = colors.first(),
                poeticDescription = poeticDescriptions[i % poeticDescriptions.size],
                colorWeights = json.encodeToString(ListSerializer(Float.serializer()), weights),
            )
        )
        monthStart = monthStart.minusMonths(1)
    }

    // Generate 3 years
    var yearStart = LocalDate.of(now.year, 1, 1)
    repeat(3) { i ->
        val yearEnd = yearStart.plusYears(1)
        val (colors, weights) = colorsAndWeightsForDate(yearStart.plusMonths(6), depth, rng)
        repo.savePalette(
            PeriodPaletteEntity(
                periodType = "YEAR",
                startDate = yearStart.toEpochDay(),
                endDate = yearEnd.toEpochDay(),
                colors = json.encodeToString(ListSerializer(String.serializer()), colors),
                photoCount = rng.nextInt(500, 3000),
                dominantColor = colors.first(),
                poeticDescription = poeticDescriptions[i % poeticDescriptions.size],
                colorWeights = json.encodeToString(ListSerializer(Float.serializer()), weights),
            )
        )
        yearStart = yearStart.minusYears(1)
    }
}

private fun colorsAndWeightsForDate(
    date: LocalDate,
    count: Int,
    rng: Random,
): Pair<List<String>, List<Float>> {
    val seasonPalette = when (date.monthValue) {
        in 3..5 -> springColors
        in 6..8 -> summerColors
        in 9..11 -> autumnColors
        else -> winterColors
    }
    val allPalettes = listOf(springColors, summerColors, autumnColors, winterColors)

    val colors = List(count) { idx ->
        if (idx < count / 2 || rng.nextFloat() > 0.3f) {
            seasonPalette[rng.nextInt(seasonPalette.size)]
        } else {
            val other = allPalettes[rng.nextInt(allPalettes.size)]
            other[rng.nextInt(other.size)]
        }
    }

    // Generate descending weights that sum to 1
    val rawWeights = List(count) { idx -> (count - idx).toFloat() + rng.nextFloat() * 2f }
    val total = rawWeights.sum()
    val weights = rawWeights.map { it / total }

    return colors to weights
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
