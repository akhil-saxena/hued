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
    content: @Composable () -> Unit,
) {
    val colorScheme = lightColorScheme(
        primary = HuedTextPrimary,
        onPrimary = HuedCanvasResting,
        secondary = HuedTextMutedResting,
        background = HuedCanvasResting,
        onBackground = HuedTextPrimary,
        surface = HuedCanvasResting,
        onSurface = HuedTextPrimary,
        surfaceVariant = HuedCanvasResting,
        onSurfaceVariant = HuedTextMutedResting,
    )

    CompositionLocalProvider(
        LocalHuedCanvas provides HuedCanvasResting,
        LocalHuedTextMuted provides HuedTextMutedResting,
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
