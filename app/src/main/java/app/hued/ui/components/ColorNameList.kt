package app.hued.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedTheme

@Composable
fun ColorNameList(
    names: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        names.zip(colors).forEach { (name, color) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun ColorNameListPreview() {
    HuedTheme {
        ColorNameList(
            names = listOf("Burnt Sienna", "Desert Sand", "Driftwood", "Peach Bloom", "Warm Clay"),
            colors = listOf(
                Color(0xFFD4764E),
                Color(0xFFC4956A),
                Color(0xFF8B6F4E),
                Color(0xFFE8A87C),
                Color(0xFFD4A574),
            ),
        )
    }
}
