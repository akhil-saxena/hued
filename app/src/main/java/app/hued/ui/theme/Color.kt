package app.hued.ui.theme

import androidx.compose.ui.graphics.Color

// Resting canvas — neutral off-white
val HuedCanvasResting = Color(0xFFF8F7F5)

// Primary text
val HuedTextPrimary = Color(0xFF2A2826)

// Muted text — resting state (no palette)
val HuedTextMutedResting = Color(0xFF8A8885)

// Muted text variants for palette tinting
val HuedTextMutedWarm = Color(0xFF8A7A6A)
val HuedTextMutedCool = Color(0xFF6A7A8A)

fun deriveCanvasTint(dominantColor: Color, restingCanvas: Color = HuedCanvasResting): Color {
    val warmth = dominantColor.red - dominantColor.blue
    val tintStrength = 0.08f

    val tinted = Color(
        red = (restingCanvas.red + warmth * tintStrength).coerceIn(0f, 1f),
        green = restingCanvas.green,
        blue = (restingCanvas.blue - warmth * tintStrength).coerceIn(0f, 1f),
        alpha = 1f,
    )

    return ContrastValidator.adjustTintForContrast(
        tintedBackground = tinted,
        textColor = HuedTextPrimary,
        restingBackground = restingCanvas,
    )
}

fun deriveMutedTextColor(dominantColor: Color): Color {
    val warmth = dominantColor.red - dominantColor.blue
    return if (warmth > 0.1f) HuedTextMutedWarm
    else if (warmth < -0.1f) HuedTextMutedCool
    else HuedTextMutedResting
}
