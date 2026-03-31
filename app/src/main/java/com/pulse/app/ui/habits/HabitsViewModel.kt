package com.pulse.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pulse.app.data.model.Habit
import com.pulse.app.data.model.HabitCompletion
import com.pulse.app.data.repository.HabitRepository
import com.pulse.app.work.HabitReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HabitWithStats(
    val habit: Habit,
    val isCompletedToday: Boolean = false,
    val currentStreak: Int = 0,
    val last7Days: List<Boolean> = List(7) { false }
)

data class HabitsUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val deletedHabit: Habit? = null
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _deletedHabit = MutableStateFlow<Habit?>(null)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.getAllActiveHabits(),
        _deletedHabit
    ) { habits, deleted ->
        val habitsWithStats = habits.map { habit ->
            val today = LocalDate.now().format(dateFormatter)
            val isCompleted = repository.isCompletedToday(habit.id, today)
            val streak = repository.getCurrentStreak(habit.id)
            val last7 = (6 downTo 0).map { daysAgo ->
                val date = LocalDate.now().minusDays(daysAgo.toLong()).format(dateFormatter)
                repository.isCompletedOnDate(habit.id, date)
            }
            HabitWithStats(
                habit = habit,
                isCompletedToday = isCompleted,
                currentStreak = streak,
                last7Days = last7
            )
        }
        HabitsUiState(
            habits = habitsWithStats,
            isLoading = false,
            deletedHabit = deleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun toggleCompletion(habitId: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().format(dateFormatter)
            repository.toggleCompletion(habitId, today)
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val id = repository.addHabit(habit)
            if (habit.notificationEnabled) {
                scheduleHabitReminders(habit.copy(id = id.toInt()))
            }
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId)
            if (habit != null) {
                _deletedHabit.value = habit
                cancelHabitWork(habitId)
                repository.deleteHabit(habitId)
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            val habit = _deletedHabit.value ?: return@launch
            repository.addHabit(habit)
            _deletedHabit.value = null
        }
    }

    fun clearDeletedHabit() {
        _deletedHabit.value = null
    }

    fun archiveHabit(habitId: Int) {
        viewModelScope.launch {
            cancelHabitWork(habitId)
            repository.archiveHabit(habitId)
        }
    }

    suspend fun getHabitStats(habitId: Int): HabitStatsData {
        val habit = repository.getHabitById(habitId) ?: return HabitStatsData()
        val currentStreak = repository.getCurrentStreak(habitId)
        val longestStreak = repository.getLongestStreak(habitId)
        val completionRate = repository.getCompletionRate(habitId, 30)
        val last5Weeks = repository.getLast5WeeksCompletions(habitId)
        return HabitStatsData(
            habit = habit,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = completionRate,
            last5WeeksCompletions = last5Weeks
        )
    }

    private fun scheduleHabitReminders(habit: Habit) {
        val days = when (habit.frequency) {
            "DAILY" -> DayOfWeek.entries.toList()
            "SPECIFIC_DAYS" -> habit.activeDays.split(",").mapNotNull { d ->
                runCatching { DayOfWeek.valueOf(d.trim()) }.getOrNull()
            }
            "X_PER_WEEK" -> DayOfWeek.entries.toList()
            else -> return
        }
        for (day in days) {
            val tag = "habit_reminder_${habit.id}_${day.name}"
            val now = ZonedDateTime.now()
            var target = now.with(TemporalAdjusters.nextOrSame(day))
                .withHour(8).withMinute(0).withSecond(0).withNano(0)
            if (target.isBefore(now)) target = target.plusWeeks(1)
            val initialDelay = Duration.between(now, target).toMillis()
            val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    "habit_id" to habit.id,
                    "habit_name" to habit.name,
                    "habit_emoji" to habit.emoji
                ))
                .addTag(tag)
                .build()
            workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        }
    }

    private fun cancelHabitWork(habitId: Int) {
        DayOfWeek.entries.forEach { day ->
            workManager.cancelUniqueWork("habit_reminder_${habitId}_${day.name}")
        }
    }
}

data class HabitStatsData(
    val habit: Habit? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionRate: Float = 0f,
    val last5WeeksCompletions: List<List<Boolean>> = emptyList()
)
