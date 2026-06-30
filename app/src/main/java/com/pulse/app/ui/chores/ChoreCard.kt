package com.pulse.app.ui.chores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChoreCard(
    choreWithWeight: ChoreWithWeight,
    modifier: Modifier = Modifier
) {
    val chore = choreWithWeight.chore
    val nowMs = System.currentTimeMillis()
    val daysSince = if (chore.lastCompletedAt == null) -1
    else ((nowMs - chore.lastCompletedAt) / 86_400_000L).toInt()

    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val (statusText, statusColor) = when {
        chore.lastCompletedAt == null -> "Never done" to errorColor
        choreWithWeight.daysOverdue > 7 -> "${choreWithWeight.daysOverdue}d overdue" to errorColor
        choreWithWeight.daysOverdue > 0 -> "${choreWithWeight.daysOverdue}d overdue" to primaryColor
        daysSince == 0 -> "Done today" to mutedColor
        else -> "${daysSince}d ago" to mutedColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = chore.emoji, fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chore.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                Text(
                    text = frequencyLabel(chore.expectedFrequencyDays),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { choreWithWeight.normalizedWeight.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(3.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

private fun frequencyLabel(days: Int): String = when (days) {
    1 -> "Daily"
    7 -> "Weekly"
    14 -> "Bi-weekly"
    30 -> "Monthly"
    else -> "Every ${days}d"
}
