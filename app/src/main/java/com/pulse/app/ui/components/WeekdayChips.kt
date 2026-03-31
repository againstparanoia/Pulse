package com.pulse.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.app.ui.theme.MutedText
import com.pulse.app.ui.theme.OnAccent
import com.pulse.app.ui.theme.PrimaryAccent
import java.time.DayOfWeek

private val dayLabels = mapOf(
    DayOfWeek.MONDAY to "M",
    DayOfWeek.TUESDAY to "T",
    DayOfWeek.WEDNESDAY to "W",
    DayOfWeek.THURSDAY to "T",
    DayOfWeek.FRIDAY to "F",
    DayOfWeek.SATURDAY to "S",
    DayOfWeek.SUNDAY to "S",
)

private val orderedDays = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

@Composable
fun WeekdayChips(
    selectedDays: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        orderedDays.forEach { day ->
            val isSelected = day in selectedDays
            WeekdayChip(
                label = dayLabels[day].orEmpty(),
                isSelected = isSelected,
                onClick = { onToggle(day) },
            )
        }
    }
}

@Composable
private fun WeekdayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "weekdayChipBg",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) OnAccent else MutedText,
        animationSpec = tween(durationMillis = 200),
        label = "weekdayChipContent",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 200),
        label = "weekdayChipBorder",
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentSize(Alignment.Center),
        )
    }
}

private fun Modifier.wrapContentSize(alignment: Alignment): Modifier =
    this.then(
        androidx.compose.foundation.layout.wrapContentSize(alignment)
    )
