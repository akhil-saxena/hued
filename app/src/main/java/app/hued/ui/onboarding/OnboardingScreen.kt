package app.hued.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedCanvasResting
import app.hued.ui.theme.HuedTextPrimary
import app.hued.ui.theme.LocalHuedTextMuted
import kotlinx.coroutines.delay

// Sample photo colors — simulates extracting from a gallery
private val samplePhotoColors = listOf(
    listOf(Color(0xFFD4764E), Color(0xFFC4956A), Color(0xFF8B6F4E)),
    listOf(Color(0xFF4A7B9D), Color(0xFF5B8FA8), Color(0xFF6B9DB8)),
    listOf(Color(0xFF4A7C59), Color(0xFF6B8F71), Color(0xFF8BA888)),
    listOf(Color(0xFFE8A87C), Color(0xFFD4A574), Color(0xFFB87333)),
    listOf(Color(0xFF8B4513), Color(0xFFA0522D), Color(0xFFCD853F)),
    listOf(Color(0xFF3D5A80), Color(0xFF4A6D8C), Color(0xFF5780A0)),
)

// The resulting palette from all "photos"
private val resultPalette = listOf(
    Color(0xFFD4764E),
    Color(0xFF4A7B9D),
    Color(0xFF4A7C59),
    Color(0xFFE8A87C),
    Color(0xFF3D5A80),
)

@Composable
fun OnboardingScreen(
    onPermissionResult: (granted: Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    // Animation phases
    var phase by remember { mutableIntStateOf(0) }
    // 0: initial — show single "photo" being extracted
    // 1: single photo extraction complete — show strip from one photo
    // 2: zoom out — show gallery grid of "photos"
    // 3: gallery transforms into a single combined palette strip
    // 4: show text + button

    var extractedBands by remember { mutableIntStateOf(0) }
    var showGallery by remember { mutableStateOf(false) }
    var galleryToStrip by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        onPermissionResult(granted)
        onComplete()
    }

    // Animation sequence
    LaunchedEffect(Unit) {
        // Phase 0→1: Extract colors from "one photo" — bands appear one by one
        delay(400)
        for (i in 1..3) {
            delay(250)
            extractedBands = i
        }
        phase = 1
        delay(600)

        // Phase 2: Show gallery grid
        showGallery = true
        phase = 2
        delay(1200)

        // Phase 3: Gallery merges into combined palette
        galleryToStrip = true
        phase = 3
        delay(800)

        // Phase 4: Show text + button
        showButton = true
        phase = 4
    }

    val galleryScale by animateFloatAsState(
        targetValue = if (galleryToStrip) 0f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "galleryScale",
    )

    val stripAlpha by animateFloatAsState(
        targetValue = if (galleryToStrip) 1f else 0f,
        animationSpec = tween(600, delayMillis = 300),
        label = "stripAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuedCanvasResting)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Phase 0-1: Single photo extraction
        if (!showGallery && !galleryToStrip) {
            // "Photo" placeholder — a colored rectangle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD4A574).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                // Simulated photo — gradient-like block
                Column {
                    Box(
                        modifier = Modifier
                            .size(160.dp, 80.dp)
                            .background(Color(0xFF87CEEB).copy(alpha = 0.5f)),
                    )
                    Box(
                        modifier = Modifier
                            .size(160.dp, 80.dp)
                            .background(Color(0xFF8B6F4E).copy(alpha = 0.5f)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Extraction result — bands appear below the photo
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp)),
            ) {
                samplePhotoColors[0].forEachIndexed { index, color ->
                    val alpha = remember { Animatable(0f) }
                    LaunchedEffect(extractedBands > index) {
                        if (extractedBands > index) {
                            alpha.animateTo(1f, animationSpec = tween(300))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(color.copy(alpha = alpha.value)),
                    )
                }
            }
        }

        // Phase 2: Gallery grid
        if (showGallery && galleryScale > 0.01f) {
            Column(
                modifier = Modifier.scale(galleryScale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (row in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0..2) {
                            val photoIdx = row * 3 + col
                            val photoColors = samplePhotoColors[photoIdx]
                            // Each "photo" as a small color block
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(photoColors[0].copy(alpha = 0.7f)),
                            ) {
                                // Mini extraction strip at bottom
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(12.dp),
                                ) {
                                    photoColors.forEach { c ->
                                        Box(modifier = Modifier.weight(1f).height(12.dp).background(c))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phase 3: Combined palette strip (fades in as gallery fades out)
        if (galleryToStrip) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .alpha(stripAlpha)
                    .clip(RoundedCornerShape(4.dp)),
            ) {
                resultPalette.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .background(color),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Phase 4: Text + button
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                // Permission button — user initiates, no auto-prompt
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
}
