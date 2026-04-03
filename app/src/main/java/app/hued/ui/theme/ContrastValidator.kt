package app.hued.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

object ContrastValidator {

    private const val AA_BODY_TEXT_RATIO = 4.5f
    private const val AA_LARGE_TEXT_RATIO = 3.0f

    fun contrastRatio(foreground: Color, background: Color): Float {
        val fgLuminance = foreground.luminance() + 0.05f
        val bgLuminance = background.luminance() + 0.05f
        return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
    }

    fun meetsBodyTextRequirement(foreground: Color, background: Color): Boolean =
        contrastRatio(foreground, background) >= AA_BODY_TEXT_RATIO

    fun meetsLargeTextRequirement(foreground: Color, background: Color): Boolean =
        contrastRatio(foreground, background) >= AA_LARGE_TEXT_RATIO

    fun adjustTintForContrast(
        tintedBackground: Color,
        textColor: Color,
        restingBackground: Color,
        minRatio: Float = AA_BODY_TEXT_RATIO,
    ): Color {
        var bg = tintedBackground
        var ratio = contrastRatio(textColor, bg)
        var blendFactor = 1.0f

        while (ratio < minRatio && blendFactor > 0f) {
            blendFactor -= 0.05f
            bg = lerp(restingBackground, tintedBackground, blendFactor.coerceAtLeast(0f))
            ratio = contrastRatio(textColor, bg)
        }
        return bg
    }

    private fun lerp(start: Color, end: Color, fraction: Float): Color = Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = 1f,
    )
}
