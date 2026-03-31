package com.pulse.app.ui.clock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.pulse.app.data.model.ReminderConfig
import com.pulse.app.ui.components.PulseButton
import com.pulse.app.ui.components.PulseTimePickerDialog
import com.pulse.app.ui.components.WeekdayChips
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (ReminderConfig) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var label by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var selectedDays by remember { mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) }
    var showTimePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "New Reminder",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                placeholder = { Text("e.g., Start of shift") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Time", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val timeStr = "%d:%02d %s".format(
                if (hour % 12 == 0) 12 else hour % 12,
                minute,
                if (hour < 12) "AM" else "PM"
            )
            androidx.compose.material3.OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(timeStr)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Active Days", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            WeekdayChips(
                selectedDays = selectedDays,
                onToggle = { day ->
                    selectedDays = if (day in selectedDays) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PulseButton(
                text = "Save Reminder",
                onClick = {
                    if (label.isNotBlank() && selectedDays.isNotEmpty()) {
                        val activeDaysStr = selectedDays.sortedBy { it.value }
                            .joinToString(",") { it.name }
                        onSave(
                            ReminderConfig(
                                label = label.trim(),
                                hour = hour,
                                minute = minute,
                                activeDays = activeDaysStr,
                                isEnabled = true
                            )
                        )
                    }
                },
                enabled = label.isNotBlank() && selectedDays.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        PulseTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
