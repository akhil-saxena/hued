package app.hued.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalHuedCanvas = compositionLocalOf { HuedCanvasResting }
val LocalHuedTextMuted = compositionLocalOf { HuedTextMutedResting }
val LocalHuedAccent = compositionLocalOf { Color.Unspecified }

@Composable
fun HuedTheme(
    paletteColors: List<Color> = emptyList(),
    content: @Composable () -> Unit,
) {
    val dominantColor = paletteColors.firstOrNull()
    val canvas = dominantColor?.let { deriveCanvasTint(it) } ?: HuedCanvasResting
    val mutedText = dominantColor?.let { deriveMutedTextColor(it) } ?: HuedTextMutedResting
    val accent = dominantColor ?: Color.Unspecified

    val colorScheme = lightColorScheme(
        primary = HuedTextPrimary,
        onPrimary = canvas,
        secondary = mutedText,
        background = canvas,
        onBackground = HuedTextPrimary,
        surface = canvas,
        onSurface = HuedTextPrimary,
        surfaceVariant = canvas,
        onSurfaceVariant = mutedText,
    )

    CompositionLocalProvider(
        LocalHuedCanvas provides canvas,
        LocalHuedTextMuted provides mutedText,
        LocalHuedAccent provides accent,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HuedTypography,
            shapes = HuedShapes,
            content = content,
        )
    }
}
