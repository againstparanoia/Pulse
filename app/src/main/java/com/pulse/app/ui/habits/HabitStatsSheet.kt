package com.pulse.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.app.ui.components.StatChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsSheet(
    habitId: Int,
    viewModel: HabitsViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var stats by remember { mutableStateOf(HabitStatsData()) }

    LaunchedEffect(habitId) {
        stats = viewModel.getHabitStats(habitId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Header with emoji + name
            if (stats.habit != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stats.habit!!.emoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text(
                        text = stats.habit!!.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Current",
                    value = "${stats.currentStreak}d",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Longest",
                    value = "${stats.longestStreak}d",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "30d Rate",
                    value = "${(stats.completionRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Heatmap
            Text(
                text = "Last 5 Weeks",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            HeatmapGrid(
                weeks = stats.last5WeeksCompletions,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
