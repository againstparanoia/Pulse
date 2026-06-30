package com.pulse.app.ui.chores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.app.ui.habits.EmojiPicker

private val FREQUENCY_PRESETS = listOf(1, 3, 7, 14, 30)
private val PRESET_LABELS = mapOf(
    1 to "Daily", 3 to "Every 3d", 7 to "Weekly", 14 to "Bi-weekly", 30 to "Monthly"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddChoreSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, frequencyDays: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartialExpansion = true)
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🧹") }
    var selectedPreset by remember { mutableIntStateOf(7) }
    var isCustom by remember { mutableStateOf(false) }
    var customDays by remember { mutableIntStateOf(7) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Chore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Chore name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text("Icon", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            EmojiPicker(
                selectedEmoji = selectedEmoji,
                onEmojiSelected = { selectedEmoji = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(Modifier.height(16.dp))

            Text("Frequency", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FREQUENCY_PRESETS.forEach { days ->
                    FilterChip(
                        selected = !isCustom && selectedPreset == days,
                        onClick = { selectedPreset = days; isCustom = false },
                        label = { Text(PRESET_LABELS[days] ?: "Every ${days}d") }
                    )
                }
                FilterChip(
                    selected = isCustom,
                    onClick = { isCustom = true },
                    label = { Text("Custom") }
                )
            }

            if (isCustom) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Every $customDays day${if (customDays != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = customDays.toFloat(),
                    onValueChange = { customDays = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 58,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val freq = if (isCustom) customDays else selectedPreset
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedEmoji, freq)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Chore")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
