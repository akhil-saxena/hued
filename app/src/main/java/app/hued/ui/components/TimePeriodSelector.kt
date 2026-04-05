package app.hued.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Time period: ${selected.name.lowercase()} selected"
            },
    ) {
        // Tab labels row — fixed height so line never shifts
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            periods.forEach { period ->
                val isSelected = period == selected
                val textAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "tabAlpha",
                )

                if (isSelected) {
                    // Selected: horizontal label + underline, takes remaining space
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(period) { detectTapGestures { onSelect(period) } },
                    ) {
                        Text(
                            text = period.name.lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .alpha(textAlpha)
                                .padding(bottom = 4.dp),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onBackground),
                        )
                    }
                } else {
                    // Non-selected: vertical label, intrinsic width
                    Box(
                        modifier = Modifier
                            .pointerInput(period) { detectTapGestures { onSelect(period) } }
                            .padding(start = 6.dp, end = 6.dp, bottom = 4.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(
                            text = period.name.lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalHuedTextMuted.current,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .alpha(textAlpha)
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(placeable.height, placeable.width) {
                                        placeable.place(
                                            x = -(placeable.width - placeable.height) / 2,
                                            y = -(placeable.height - placeable.width) / 2,
                                        )
                                    }
                                }
                                .rotate(-90f),
                        )
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun TimePeriodSelectorPreviewWeek() {
    HuedTheme {
        TimePeriodSelector(selected = TimePeriod.WEEK, onSelect = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun TimePeriodSelectorPreviewMonth() {
    HuedTheme {
        TimePeriodSelector(selected = TimePeriod.MONTH, onSelect = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F7F5)
@Composable
private fun TimePeriodSelectorPreviewYear() {
    HuedTheme {
        TimePeriodSelector(selected = TimePeriod.YEAR, onSelect = {})
    }
}
