package app.hued.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun PoeticDescription(
    text: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    Text(
        text = text,
        style = if (expanded) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        color = LocalHuedTextMuted.current,
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun PoeticDescriptionPreview() {
    HuedTheme {
        PoeticDescription(text = "Warm days finding their way back in.")
    }
}
