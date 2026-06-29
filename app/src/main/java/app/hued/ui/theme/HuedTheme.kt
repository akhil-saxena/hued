package app.hued.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val canvas = if (darkTheme) HuedCanvasDark else HuedCanvasResting
    val textPrimary = if (darkTheme) HuedTextPrimaryDark else HuedTextPrimary
    val textMuted = if (darkTheme) HuedTextMutedDark else HuedTextMutedResting

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = textPrimary,
            onPrimary = canvas,
            secondary = textMuted,
            background = canvas,
            onBackground = textPrimary,
            surface = canvas,
            onSurface = textPrimary,
            surfaceVariant = canvas,
            onSurfaceVariant = textMuted,
        )
    } else {
        lightColorScheme(
            primary = textPrimary,
            onPrimary = canvas,
            secondary = textMuted,
            background = canvas,
            onBackground = textPrimary,
            surface = canvas,
            onSurface = textPrimary,
            surfaceVariant = canvas,
            onSurfaceVariant = textMuted,
        )
    }

    CompositionLocalProvider(
        LocalHuedCanvas provides canvas,
        LocalHuedTextMuted provides textMuted,
        LocalHuedAccent provides Color.Unspecified,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HuedTypography,
            shapes = HuedShapes,
            content = content,
        )
    }
}
