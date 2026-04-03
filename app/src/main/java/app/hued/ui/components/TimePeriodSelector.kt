package app.hued.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.hued.data.model.TimePeriod
import app.hued.ui.theme.HuedTheme
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun TimePeriodSelector(
    selected: TimePeriod,
    onSelect: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = TimePeriod.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Time period: ${selected.name.lowercase()} selected"
            },
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        periods.forEach { period ->
            val isSelected = period == selected
            val label = period.name.lowercase()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(role = Role.Tab) { onSelect(period) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .semantics {
                        role = Role.Tab
                        this.selected = isSelected
                    },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        LocalHuedTextMuted.current
                    },
                )
                if (isSelected) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun TimePeriodSelectorPreview() {
    HuedTheme {
        TimePeriodSelector(
            selected = TimePeriod.MONTH,
            onSelect = {},
        )
    }
}
