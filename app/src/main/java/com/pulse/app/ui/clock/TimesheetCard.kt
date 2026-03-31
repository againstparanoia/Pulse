package com.pulse.app.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.app.data.model.ClockEvent
import com.pulse.app.ui.components.CollapsibleCard
import com.pulse.app.ui.components.PulseTimePickerDialog
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun TimesheetCard(
    events: List<ClockEvent>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    weekTotalHours: Float,
    onUpdateEvent: (ClockEvent) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<ClockEvent?>(null) }
    var editHour by remember { mutableIntStateOf(0) }
    var editMinute by remember { mutableIntStateOf(0) }

    CollapsibleCard(
        title = "This Week",
        isExpanded = isExpanded,
        onToggle = onToggle
    ) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dayFormatter = DateTimeFormatter.ofPattern("EEE")
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Day", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("In", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Out", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hours", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.7f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            for (i in 0..6) {
                val day = monday.plusDays(i.toLong())
                val dayEvents = events.filter { event ->
                    Instant.ofEpochMilli(event.timestamp).atZone(zone).toLocalDate() == day
                }.sortedBy { it.timestamp }

                val clockIns = dayEvents.filter { it.type == "CLOCK_IN" }
                val clockOuts = dayEvents.filter { it.type == "CLOCK_OUT" }
                val dayHours = calculateDayHours(dayEvents)
                val highlight = dayHours >= 8f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (highlight) Modifier.background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) else Modifier
                        )
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        day.format(dayFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                    )

                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        val firstIn = clockIns.firstOrNull()
                        if (firstIn != null) {
                            Text(
                                Instant.ofEpochMilli(firstIn.timestamp).atZone(zone).format(timeFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable {
                                    editingEvent = firstIn
                                    val zdt = Instant.ofEpochMilli(firstIn.timestamp).atZone(zone)
                                    editHour = zdt.hour
                                    editMinute = zdt.minute
                                    showTimePicker = true
                                }
                            )
                            if (firstIn.isManualEdit) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edited",
                                    modifier = Modifier.padding(start = 2.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        val firstOut = clockOuts.firstOrNull()
                        if (firstOut != null) {
                            Text(
                                Instant.ofEpochMilli(firstOut.timestamp).atZone(zone).format(timeFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable {
                                    editingEvent = firstOut
                                    val zdt = Instant.ofEpochMilli(firstOut.timestamp).atZone(zone)
                                    editHour = zdt.hour
                                    editMinute = zdt.minute
                                    showTimePicker = true
                                }
                            )
                            if (firstOut.isManualEdit) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edited",
                                    modifier = Modifier.padding(start = 2.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text("—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        "%.1f".format(dayHours),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Week Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val totalColor = when {
                    weekTotalHours > 40f -> Color(0xFFF59E0B)
                    weekTotalHours >= 35f -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    "%.1f hrs".format(weekTotalHours),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = totalColor
                )
            }
        }
    }

    if (showTimePicker && editingEvent != null) {
        PulseTimePickerDialog(
            initialHour = editHour,
            initialMinute = editMinute,
            onConfirm = { h, m ->
                val event = editingEvent!!
                val zone = ZoneId.systemDefault()
                val original = Instant.ofEpochMilli(event.timestamp).atZone(zone)
                val updated = original.withHour(h).withMinute(m).withSecond(0).withNano(0)
                onUpdateEvent(event.copy(
                    timestamp = updated.toInstant().toEpochMilli(),
                    isManualEdit = true
                ))
                showTimePicker = false
                editingEvent = null
            },
            onDismiss = {
                showTimePicker = false
                editingEvent = null
            }
        )
    }
}

private fun calculateDayHours(events: List<ClockEvent>): Float {
    val sorted = events.sortedBy { it.timestamp }
    var totalMs = 0L
    var i = 0
    while (i < sorted.size - 1) {
        if (sorted[i].type == "CLOCK_IN" && sorted[i + 1].type == "CLOCK_OUT") {
            totalMs += sorted[i + 1].timestamp - sorted[i].timestamp
            i += 2
        } else {
            i++
        }
    }
    return totalMs / 3_600_000f
}
