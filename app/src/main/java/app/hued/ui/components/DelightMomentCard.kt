package app.hued.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.hued.data.model.DelightMoment
import app.hued.ui.theme.HuedTheme

@Composable
fun DelightMomentCard(
    moment: DelightMoment,
    modifier: Modifier = Modifier,
) {
    Text(
        text = moment.text,
        style = when (moment) {
            is DelightMoment.Monochrome -> MaterialTheme.typography.headlineMedium
            is DelightMoment.NewYear -> MaterialTheme.typography.headlineMedium
            else -> MaterialTheme.typography.bodyMedium
        },
        textAlign = when (moment) {
            is DelightMoment.Monochrome, is DelightMoment.NewYear -> TextAlign.Center
            else -> TextAlign.Start
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun DelightMomentMonochromePreview() {
    HuedTheme {
        DelightMomentCard(
            moment = DelightMoment.Monochrome(
                text = "A month in blue. Nothing but blue.",
                periodKey = "MONTH_2026-02",
                hueName = "blue",
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun DelightMomentHarmonyPreview() {
    HuedTheme {
        DelightMomentCard(
            moment = DelightMoment.Harmony(
                text = "Perfectly balanced. Warm and cool in equal measure.",
                periodKey = "MONTH_2026-03",
            ),
        )
    }
}
