package app.hued.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedTheme

@Composable
fun PaletteStrip(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    cornerRadius: Dp = 4.dp,
    colorNames: List<String> = emptyList(),
    colorWeights: List<Float> = emptyList(),
    useWeightedBands: Boolean = false,
) {
    val description = if (colorNames.isNotEmpty()) {
        "Palette: ${colorNames.joinToString(", ")}"
    } else {
        "Palette: ${colors.size} colors"
    }

    if (colors.isEmpty()) return

    val hasValidWeights = useWeightedBands &&
        colorWeights.size == colors.size &&
        colorWeights.all { it > 0f }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .semantics { contentDescription = description },
    ) {
        colors.forEachIndexed { index, color ->
            val weight = if (hasValidWeights) colorWeights[index] else 1f
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(height)
                    .background(color),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PaletteStripPreview() {
    HuedTheme {
        PaletteStrip(
            colors = listOf(
                Color(0xFFD4764E), Color(0xFFC4956A), Color(0xFF8B6F4E),
                Color(0xFFE8A87C), Color(0xFFD4A574),
            ),
            height = 80.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PaletteStripWeightedPreview() {
    HuedTheme {
        PaletteStrip(
            colors = listOf(
                Color(0xFFD4764E), Color(0xFFC4956A), Color(0xFF8B6F4E),
                Color(0xFFE8A87C), Color(0xFFD4A574),
            ),
            colorWeights = listOf(0.4f, 0.25f, 0.15f, 0.12f, 0.08f),
            useWeightedBands = true,
            height = 80.dp,
        )
    }
}
