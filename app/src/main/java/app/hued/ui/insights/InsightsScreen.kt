package app.hued.ui.insights

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.hued.R
import app.hued.data.model.TimePeriod
import app.hued.data.repository.PaletteRepository
import app.hued.processing.ColorNamer
import app.hued.ui.components.PaletteStrip
import app.hued.ui.components.PillButton
import app.hued.ui.theme.LocalHuedTextMuted
import app.hued.util.toComposeColor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InsightsEntryPoint {
    fun paletteRepository(): PaletteRepository
    fun colorNamer(): ColorNamer
}

private data class InsightsData(
    val favoriteColorHex: String?,
    val favoriteColorName: String?,
    val totalPhotos: Int,
    val weeksTracked: Int,
    val yearColorsHex: List<String>,
    val yearColorNames: List<String>,
)

@Composable
fun InsightsScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, InsightsEntryPoint::class.java)
    }
    val repository = remember { entryPoint.paletteRepository() }
    val colorNamer = remember { entryPoint.colorNamer() }

    var data by remember { mutableStateOf<InsightsData?>(null) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            val json = Json { ignoreUnknownKeys = true }

            val favoriteHex = repository.getFavoriteColor()
            val favoriteName = favoriteHex?.let { colorNamer.getName(it) }
            val totalPhotos = repository.getTotalPhotoCount()
            val weeksTracked = repository.getWeekCount()

            val yearPalette = repository.getLatestPalette(TimePeriod.YEAR).first()
            val yearColors: List<String> = yearPalette?.colors?.let { raw ->
                runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
            } ?: emptyList()
            val yearColorNames = yearColors.map { colorNamer.getName(it) }

            InsightsData(
                favoriteColorHex = favoriteHex,
                favoriteColorName = favoriteName,
                totalPhotos = totalPhotos,
                weeksTracked = weeksTracked,
                yearColorsHex = yearColors,
                yearColorNames = yearColorNames,
            )
        }
        data = loaded
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.insights_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            PillButton(
                text = stringResource(R.string.close),
                onClick = onClose,
                color = LocalHuedTextMuted.current,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val current = data
        if (current == null) {
            // Subtle loading state
            Text(
                text = "…",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalHuedTextMuted.current.copy(alpha = 0.4f),
            )
        } else {
            // ── MOST-SEEN COLOR ──
            Text(
                text = stringResource(R.string.favorite_color),
                style = MaterialTheme.typography.labelMedium,
                color = LocalHuedTextMuted.current,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            current.favoriteColorHex?.toComposeColor()
                                ?: LocalHuedTextMuted.current.copy(alpha = 0.15f),
                        ),
                )
                Text(
                    text = current.favoriteColorName
                        ?: stringResource(R.string.insights_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── STAT ROWS ──
            StatRow(
                label = stringResource(R.string.total_photos),
                value = current.totalPhotos.toString(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow(
                label = stringResource(R.string.weeks_tracked),
                value = current.weeksTracked.toString(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── YEAR IN COLOR ──
            Text(
                text = stringResource(R.string.palette_of_the_year),
                style = MaterialTheme.typography.labelMedium,
                color = LocalHuedTextMuted.current,
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (current.yearColorsHex.isNotEmpty()) {
                PaletteStrip(
                    colors = current.yearColorsHex.map { it.toComposeColor() },
                    height = 64.dp,
                    colorNames = current.yearColorNames,
                )
                Spacer(modifier = Modifier.height(16.dp))
                current.yearColorsHex.forEachIndexed { index, hex ->
                    ColorNameRow(
                        color = hex.toComposeColor(),
                        name = current.yearColorNames.getOrElse(index) { hex },
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalHuedTextMuted.current.copy(alpha = 0.4f),
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalHuedTextMuted.current,
        )
    }
}

@Composable
private fun ColorNameRow(color: Color, name: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
