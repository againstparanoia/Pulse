package com.pulse.app.ui.habits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.app.data.model.Habit
import com.pulse.app.ui.components.PulseButton
import com.pulse.app.ui.components.WeekdayChips
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("💧") }
    var frequency by remember { mutableStateOf("DAILY") }
    var frequencyCount by remember { mutableFloatStateOf(3f) }
    var activeDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var notificationEnabled by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Habit name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji picker
            Text(
                text = "Choose an icon",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            EmojiPicker(
                selectedEmoji = emoji,
                onEmojiSelected = { emoji = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                listOf("DAILY" to "Daily", "X_PER_WEEK" to "X/Week", "SPECIFIC_DAYS" to "Specific Days").forEach { (value, label) ->
                    FilterChip(
                        selected = frequency == value,
                        onClick = { frequency = value },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // X per week slider
            if (frequency == "X_PER_WEEK") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${frequencyCount.toInt()} times per week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = frequencyCount,
                    onValueChange = { frequencyCount = it },
                    valueRange = 1f..7f,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Specific days picker
            if (frequency == "SPECIFIC_DAYS") {
                Spacer(modifier = Modifier.height(8.dp))
                WeekdayChips(
                    selectedDays = activeDays,
                    onToggle = { day ->
                        activeDays = if (activeDays.contains(day)) {
                            activeDays - day
                        } else {
                            activeDays + day
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminders",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Get notified to complete this habit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            PulseButton(
                text = "Create Habit",
                onClick = {
                    if (name.isNotBlank()) {
                        val habit = Habit(
                            name = name.trim(),
                            emoji = emoji,
                            frequency = frequency,
                            frequencyCount = frequencyCount.toInt(),
                            activeDays = activeDays.joinToString(",") { it.name },
                            notificationEnabled = notificationEnabled
                        )
                        onSave(habit)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
