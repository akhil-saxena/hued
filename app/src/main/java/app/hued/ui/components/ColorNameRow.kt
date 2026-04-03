package app.hued.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun ColorNameRow(
    names: List<String>,
    modifier: Modifier = Modifier,
) {
    val mutedColor = LocalHuedTextMuted.current

    Row(modifier = modifier.fillMaxWidth()) {
        names.forEach { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = mutedColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun ColorNameRowPreview() {
    HuedTheme {
        ColorNameRow(
            names = listOf("Burnt Sienna", "Desert Sand", "Driftwood", "Peach Bloom", "Warm Clay"),
        )
    }
}
