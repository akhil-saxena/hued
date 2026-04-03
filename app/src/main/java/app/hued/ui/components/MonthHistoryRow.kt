package app.hued.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun MonthHistoryRow(
    monthLabel: String,
    colors: List<Color>,
    colorNames: List<String>,
    poeticDescription: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    streakText: String? = null,
    favoriteColor: String? = null,
    onShareClick: (() -> Unit)? = null,
) {
    val talkbackDesc = if (isExpanded) {
        "$monthLabel palette expanded"
    } else {
        "$monthLabel palette. Tap to expand."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = talkbackDesc },
    ) {
        // Collapsed row: label + compact strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelLarge,
                color = LocalHuedTextMuted.current,
                modifier = Modifier.width(100.dp),
            )
            PaletteStrip(
                colors = colors,
                height = 40.dp,
                cornerRadius = 3.dp,
                colorNames = colorNames,
                modifier = Modifier.weight(1f),
            )
        }

        // Expanded detail
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300, delayMillis = 150)),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200)),
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                PaletteStrip(
                    colors = colors,
                    height = 120.dp,
                    colorNames = colorNames,
                )
                Spacer(modifier = Modifier.height(12.dp))
                ColorNameList(
                    names = colorNames,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
                PoeticDescription(
                    text = poeticDescription,
                    expanded = true,
                )
                streakText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalHuedTextMuted.current,
                    )
                }
                favoriteColor?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your favorite: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalHuedTextMuted.current,
                    )
                }
                if (onShareClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "share",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable(onClick = onShareClick)
                            .padding(vertical = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun MonthHistoryRowCollapsedPreview() {
    HuedTheme {
        MonthHistoryRow(
            monthLabel = "March 2026",
            colors = listOf(Color(0xFF4A7B9D), Color(0xFF5B8FA8), Color(0xFF6B9DB8), Color(0xFF7BABC4), Color(0xFF8BB8D0)),
            colorNames = listOf("Distant Horizon", "Still Water", "Morning Fog", "Pale Shore", "Winter Sky"),
            poeticDescription = "The quiet after everything settled.",
            isExpanded = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun MonthHistoryRowExpandedPreview() {
    HuedTheme {
        MonthHistoryRow(
            monthLabel = "March 2026",
            colors = listOf(Color(0xFF4A7B9D), Color(0xFF5B8FA8), Color(0xFF6B9DB8), Color(0xFF7BABC4), Color(0xFF8BB8D0)),
            colorNames = listOf("Distant Horizon", "Still Water", "Morning Fog", "Pale Shore", "Winter Sky"),
            poeticDescription = "The quiet after everything settled.",
            isExpanded = true,
            onClick = {},
            streakText = "3 weeks of cool tones and counting",
        )
    }
}
