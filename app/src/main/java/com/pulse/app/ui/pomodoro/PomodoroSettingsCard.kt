package com.pulse.app.ui.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.app.data.model.PomodoroSettings
import com.pulse.app.ui.components.CollapsibleCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PomodoroSettingsCard(
    settings: PomodoroSettings,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onUpdateSettings: (PomodoroSettings) -> Unit
) {
    CollapsibleCard(
        title = "Settings",
        isExpanded = isExpanded,
        onToggle = onToggle
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            var focusValue by remember(settings.focusMinutes) { mutableFloatStateOf(settings.focusMinutes.toFloat()) }
            var shortBreakValue by remember(settings.shortBreakMinutes) { mutableFloatStateOf(settings.shortBreakMinutes.toFloat()) }
            var longBreakValue by remember(settings.longBreakMinutes) { mutableFloatStateOf(settings.longBreakMinutes.toFloat()) }

            SettingsSlider(
                label = "Focus Duration",
                value = focusValue,
                onValueChange = { focusValue = it },
                onValueChangeFinished = {
                    onUpdateSettings(settings.copy(focusMinutes = focusValue.toInt()))
                },
                valueRange = 5f..90f,
                steps = 16,
                displayValue = "${focusValue.toInt()} min"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSlider(
                label = "Short Break",
                value = shortBreakValue,
                onValueChange = { shortBreakValue = it },
                onValueChangeFinished = {
                    onUpdateSettings(settings.copy(shortBreakMinutes = shortBreakValue.toInt()))
                },
                valueRange = 1f..30f,
                steps = 28,
                displayValue = "${shortBreakValue.toInt()} min"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSlider(
                label = "Long Break",
                value = longBreakValue,
                onValueChange = { longBreakValue = it },
                onValueChangeFinished = {
                    onUpdateSettings(settings.copy(longBreakMinutes = longBreakValue.toInt()))
                },
                valueRange = 5f..60f,
                steps = 10,
                displayValue = "${longBreakValue.toInt()} min"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Sessions per Cycle", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (count in 1..8) {
                    FilterChip(
                        selected = settings.sessionsPerCycle == count,
                        onClick = {
                            onUpdateSettings(settings.copy(sessionsPerCycle = count))
                        },
                        label = { Text("$count") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                displayValue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
