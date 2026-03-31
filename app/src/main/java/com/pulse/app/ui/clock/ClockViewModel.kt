package com.pulse.app.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pulse.app.data.model.ClockEvent
import com.pulse.app.data.model.ReminderConfig
import com.pulse.app.data.repository.ClockRepository
import com.pulse.app.work.ClockReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ClockUiState(
    val isClockedIn: Boolean = false,
    val currentSessionStartMs: Long? = null,
    val weekEvents: List<ClockEvent> = emptyList(),
    val reminders: List<ReminderConfig> = emptyList(),
    val isWeekExpanded: Boolean = false,
    val weekTotalHours: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val repository: ClockRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _expandedState = MutableStateFlow(false)

    private val weekStart: Long
        get() {
            val now = LocalDate.now()
            val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            return monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

    private val weekEnd: Long
        get() {
            val now = LocalDate.now()
            val sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            return sunday.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

    val uiState: StateFlow<ClockUiState> = combine(
        repository.isClockedIn(),
        repository.getLatestClockIn(),
        repository.getWeekEvents(weekStart, weekEnd),
        repository.getAllReminders(),
        _expandedState
    ) { isClockedIn, latestEvent, weekEvents, reminders, expanded ->
        val totalHours = calculateWeekTotalHours(weekEvents)
        ClockUiState(
            isClockedIn = isClockedIn,
            currentSessionStartMs = if (isClockedIn) latestEvent?.timestamp else null,
            weekEvents = weekEvents,
            reminders = reminders,
            isWeekExpanded = expanded,
            weekTotalHours = totalHours,
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClockUiState()
    )

    fun clockIn() {
        viewModelScope.launch {
            try {
                repository.clockIn()
            } catch (e: Exception) {
                // Error handled via state
            }
        }
    }

    fun clockOut() {
        viewModelScope.launch {
            try {
                repository.clockOut()
            } catch (e: Exception) {
                // Error handled via state
            }
        }
    }

    fun toggleWeekExpanded() {
        _expandedState.update { !it }
    }

    fun updateEvent(event: ClockEvent) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isManualEdit = true))
        }
    }

    fun addReminder(config: ReminderConfig) {
        viewModelScope.launch {
            val id = repository.addReminder(config)
            if (config.isEnabled) {
                scheduleReminder(config.copy(id = id.toInt()))
            }
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            cancelReminderWork(id)
            repository.deleteReminder(id)
        }
    }

    fun toggleReminder(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, enabled)
            val reminder = uiState.value.reminders.find { it.id == id } ?: return@launch
            if (enabled) {
                scheduleReminder(reminder.copy(isEnabled = true))
            } else {
                cancelReminderWork(id)
            }
        }
    }

    private fun scheduleReminder(config: ReminderConfig) {
        val days = config.activeDays.split(",").mapNotNull { dayStr ->
            runCatching { DayOfWeek.valueOf(dayStr.trim()) }.getOrNull()
        }
        for (day in days) {
            val tag = "clock_reminder_${config.id}_${day.name}"
            val now = ZonedDateTime.now()
            var target = now.with(TemporalAdjusters.nextOrSame(day))
                .withHour(config.hour)
                .withMinute(config.minute)
                .withSecond(0)
                .withNano(0)
            if (target.isBefore(now)) {
                target = target.plusWeeks(1)
            }
            val initialDelay = Duration.between(now, target).toMillis()
            val workRequest = PeriodicWorkRequestBuilder<ClockReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    "label" to config.label,
                    "reminder_id" to config.id
                ))
                .addTag(tag)
                .build()
            workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    private fun cancelReminderWork(reminderId: Int) {
        DayOfWeek.entries.forEach { day ->
            workManager.cancelUniqueWork("clock_reminder_${reminderId}_${day.name}")
        }
    }

    private fun calculateWeekTotalHours(events: List<ClockEvent>): Float {
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
}
