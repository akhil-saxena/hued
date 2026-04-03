package app.hued.ui.components

import androidx.compose.foundation.background
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
) {
    val description = if (colorNames.isNotEmpty()) {
        "Palette: ${colorNames.joinToString(", ")}"
    } else {
        "Palette: ${colors.size} colors"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .semantics { contentDescription = description },
    ) {
        colors.forEach { color ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(color),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PaletteStripHeroPreview() {
    HuedTheme {
        PaletteStrip(
            colors = listOf(
                Color(0xFFD4764E),
                Color(0xFFC4956A),
                Color(0xFF8B6F4E),
                Color(0xFFE8A87C),
                Color(0xFFD4A574),
            ),
            colorNames = listOf("Burnt Sienna", "Desert Sand", "Driftwood", "Peach Bloom", "Warm Clay"),
            height = 80.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PaletteStripExpandedPreview() {
    HuedTheme {
        PaletteStrip(
            colors = listOf(
                Color(0xFF4A7B9D),
                Color(0xFF5B8FA8),
                Color(0xFF6B9DB8),
                Color(0xFF7BABC4),
                Color(0xFF8BB8D0),
            ),
            height = 120.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PaletteStripHistoryPreview() {
    HuedTheme {
        PaletteStrip(
            colors = listOf(
                Color(0xFF3D5A80),
                Color(0xFF4A6D8C),
                Color(0xFF5780A0),
                Color(0xFF6493B4),
            ),
            height = 40.dp,
            cornerRadius = 3.dp,
        )
    }
}
