package app.hued.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun ComparisonView(
    labelA: String,
    colorsA: List<Color>,
    labelB: String,
    colorsB: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Comparing $labelA with $labelB"
            },
    ) {
        Text(
            text = labelA,
            style = MaterialTheme.typography.labelLarge,
            color = LocalHuedTextMuted.current,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PaletteStrip(
            colors = colorsA,
            height = 80.dp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = labelB,
            style = MaterialTheme.typography.labelLarge,
            color = LocalHuedTextMuted.current,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PaletteStrip(
            colors = colorsB,
            height = 80.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun ComparisonViewPreview() {
    HuedTheme {
        ComparisonView(
            labelA = "April 2026",
            colorsA = listOf(Color(0xFFD4764E), Color(0xFFC4956A), Color(0xFF8B6F4E), Color(0xFFE8A87C)),
            labelB = "March 2026",
            colorsB = listOf(Color(0xFF4A7B9D), Color(0xFF5B8FA8), Color(0xFF6B9DB8), Color(0xFF7BABC4)),
            modifier = Modifier.padding(24.dp),
        )
    }
}
