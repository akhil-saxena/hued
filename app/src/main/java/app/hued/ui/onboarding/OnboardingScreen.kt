package app.hued.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedCanvasResting
import app.hued.ui.theme.HuedTextPrimary
import app.hued.ui.theme.LocalHuedTextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 5 visually distinct swatches — warm, cool, natural, soft, golden
// dominantIndex per swatch varies position so selection feels organic
private val swatchColors = listOf(
    // Warm terracotta family — dominant top-left
    listOf(Color(0xFFCB6D51), Color(0xFFD4956A), Color(0xFFE8B89A), Color(0xFFA85A3C)),
    // Cool ocean family — dominant bottom-right
    listOf(Color(0xFF5B93B5), Color(0xFF85B8D4), Color(0xFF2C526B), Color(0xFF3B6B8A)),
    // Natural forest family — dominant top-right
    listOf(Color(0xFF72A67E), Color(0xFF4A7C59), Color(0xFF9DC4A5), Color(0xFF3A6145)),
    // Dusty lavender family — dominant bottom-left
    listOf(Color(0xFFB8A9C9), Color(0xFF9882B0), Color(0xFF7B6897), Color(0xFFC4B8D6)),
    // Warm amber family — dominant bottom-right
    listOf(Color(0xFFD4A24E), Color(0xFFE8C476), Color(0xFFBF8A32), Color(0xFFC49540)),
)

// Which cube index is "dominant" per swatch (varies the pick position)
private val dominantIndices = listOf(0, 3, 1, 2, 3)

// The 5 dominant colors form the palette
private val resultPalette = listOf(
    Color(0xFFCB6D51),
    Color(0xFF3B6B8A),
    Color(0xFF4A7C59),
    Color(0xFF7B6897),
    Color(0xFFC49540),
)

private val SWATCH_COUNT = 5
private val SWATCH_SIZE = 64.dp
private val INNER_GAP = 3.dp
private val INNER_RADIUS = 3.dp
private val OUTER_RADIUS = 6.dp

@Composable
fun OnboardingScreen(
    onPermissionResult: (granted: Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    var showSwatches by remember { mutableStateOf(false) }
    var selectingIndex by remember { mutableIntStateOf(-1) }
    var landedBands by remember { mutableIntStateOf(0) }
    var hideSwatches by remember { mutableStateOf(false) }

    // Per-swatch stagger appearance
    val swatchAppeared = remember { List(SWATCH_COUNT) { Animatable(0f) } }
    // Per-swatch: non-dominant fade out
    val nonDominantAlpha = remember { List(SWATCH_COUNT) { Animatable(1f) } }
    // Per-swatch: dominant scale up
    val dominantScale = remember { List(SWATCH_COUNT) { Animatable(1f) } }
    // Per-band: strip band alpha
    val bandAlpha = remember { List(SWATCH_COUNT) { Animatable(0f) } }
    // Swatch area fade-out
    val swatchAreaAlpha = remember { Animatable(1f) }
    // Phase 3: text + button entrance
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(40f) }
    // Footer wordmark
    val footerAlpha = remember { Animatable(0f) }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        onPermissionResult(granted)
        onComplete()
    }

    LaunchedEffect(Unit) {
        delay(400)

        // Phase 1: Swatches appear with stagger
        showSwatches = true
        for (i in 0 until SWATCH_COUNT) {
            launch { swatchAppeared[i].animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
            delay(100)
        }
        delay(600)

        // Phase 2: Select dominant from each swatch, one at a time
        for (i in 0 until SWATCH_COUNT) {
            selectingIndex = i

            // Non-dominant cubes fade out, dominant scales up
            launch { nonDominantAlpha[i].animateTo(0.15f, tween(500)) }
            launch { dominantScale[i].animateTo(1.15f, tween(400, easing = FastOutSlowInEasing)) }
            delay(350)

            // Band appears in strip
            launch { bandAlpha[i].animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
            landedBands = i + 1
            delay(300)

            // Dominant settles back
            launch { dominantScale[i].animateTo(1f, tween(250)) }
            delay(250)
        }

        delay(500)

        // Phase 3: Swatches fade out, CTA fades/slides in, footer appears
        launch { swatchAreaAlpha.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        delay(300)
        launch { ctaAlpha.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        launch { ctaOffsetY.animateTo(0f, tween(900, easing = FastOutSlowInEasing)) }
        launch { footerAlpha.animateTo(1f, tween(1000, delayMillis = 200, easing = FastOutSlowInEasing)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HuedCanvasResting),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Phase 1-2: Swatches (kept in layout tree to avoid jump on fade-out)
            if (showSwatches) {
                Row(
                    modifier = Modifier.alpha(swatchAreaAlpha.value),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (i in 0 until SWATCH_COUNT) {
                        SwatchCell(
                            colors = swatchColors[i],
                            dominantIndex = dominantIndices[i],
                            appeared = swatchAppeared[i].value,
                            nonDominantAlpha = nonDominantAlpha[i].value,
                            dominantScale = dominantScale[i].value,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Palette strip — bands appear as they're selected
            if (landedBands > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(OUTER_RADIUS)),
                ) {
                    resultPalette.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .alpha(bandAlpha[index].value)
                                .background(color),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // Phase 3: Text + button (manual animation — no AnimatedVisibility)
            if (ctaAlpha.value > 0.01f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(ctaAlpha.value)
                        .offset { IntOffset(0, ctaOffsetY.value.dp.roundToPx()) },
                ) {
                    Text(
                        text = "your life in color",
                        style = MaterialTheme.typography.displaySmall,
                        color = HuedTextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All processing happens on your device.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalHuedTextMuted.current,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HuedTextPrimary)
                            .clickable {
                                val permissions = when {
                                    Build.VERSION.SDK_INT >= 34 -> arrayOf(
                                        Manifest.permission.READ_MEDIA_IMAGES,
                                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                                    )
                                    Build.VERSION.SDK_INT >= 33 -> arrayOf(
                                        Manifest.permission.READ_MEDIA_IMAGES,
                                    )
                                    else -> arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                    )
                                }
                                galleryPermissionLauncher.launch(permissions)
                            }
                            .padding(horizontal = 32.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "show me my colors",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HuedCanvasResting,
                        )
                    }
                }
            }
        }

        // Footer wordmark — appears with CTA
        if (footerAlpha.value > 0.01f) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(footerAlpha.value)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "hued",
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalHuedTextMuted.current,
                )
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalHuedTextMuted.current.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun SwatchCell(
    colors: List<Color>,
    dominantIndex: Int,
    appeared: Float,
    nonDominantAlpha: Float,
    dominantScale: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(SWATCH_SIZE)
            .scale(appeared)
            .alpha(appeared)
            .clip(RoundedCornerShape(OUTER_RADIUS)),
        verticalArrangement = Arrangement.spacedBy(INNER_GAP),
    ) {
        for (row in 0..1) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(INNER_GAP),
            ) {
                for (col in 0..1) {
                    val idx = row * 2 + col
                    val isDominant = idx == dominantIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .then(
                                if (isDominant) Modifier.scale(dominantScale)
                                else Modifier.alpha(nonDominantAlpha)
                            )
                            .clip(RoundedCornerShape(INNER_RADIUS))
                            .background(colors.getOrElse(idx) { Color.Gray }),
                    )
                }
            }
        }
    }
}
