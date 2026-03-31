package com.pulse.app.data.repository

import com.pulse.app.data.db.HabitCompletionDao
import com.pulse.app.data.db.HabitDao
import com.pulse.app.data.model.Habit
import com.pulse.app.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Habit CRUD

    fun getAllActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits()
    }

    fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits()
    }

    fun getArchivedHabits(): Flow<List<Habit>> {
        return habitDao.getArchivedHabits()
    }

    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getById(habitId)
    }

    suspend fun addHabit(habit: Habit): Long {
        return habitDao.insert(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit)
    }

    suspend fun deleteHabit(habitId: Int) {
        habitDao.deleteById(habitId)
    }

    suspend fun archiveHabit(habitId: Int, archived: Boolean = true) {
        habitDao.setArchived(habitId, archived)
    }

    // Completions

    suspend fun isCompletedToday(habitId: Int, date: String): Boolean {
        return habitCompletionDao.getCompletion(habitId, date) != null
    }

    suspend fun isCompletedOnDate(habitId: Int, date: String): Boolean {
        return habitCompletionDao.getCompletion(habitId, date) != null
    }

    suspend fun toggleCompletion(habitId: Int, date: String) {
        val existing = habitCompletionDao.getCompletion(habitId, date)
        if (existing != null) {
            habitCompletionDao.delete(habitId, date)
        } else {
            habitCompletionDao.insert(
                HabitCompletion(
                    habitId = habitId,
                    completedDate = date
                )
            )
        }
    }

    fun getCompletionsForHabit(
        habitId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletions(habitId, startDate, endDate)
    }

    suspend fun getCurrentStreak(habitId: Int): Int {
        val completionDates = habitCompletionDao.getAllCompletionDates(habitId)
        if (completionDates.isEmpty()) return 0

        val sortedDates = completionDates.map { LocalDate.parse(it, dateFormatter) }.sorted().reversed()
        val today = LocalDate.now()

        // Streak must include today or yesterday to be active
        val firstDate = sortedDates.first()
        if (ChronoUnit.DAYS.between(firstDate, today) > 1) return 0

        var streak = 1
        for (i in 1 until sortedDates.size) {
            val diff = ChronoUnit.DAYS.between(sortedDates[i], sortedDates[i - 1])
            if (diff == 1L) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    suspend fun getLongestStreak(habitId: Int): Int {
        val completionDates = habitCompletionDao.getAllCompletionDates(habitId)
        if (completionDates.isEmpty()) return 0

        val sortedDates = completionDates.map { LocalDate.parse(it, dateFormatter) }.sorted()

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val diff = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
            if (diff == 1L) {
                currentStreak++
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            } else if (diff > 1L) {
                currentStreak = 1
            }
            // diff == 0 means duplicate date, skip without resetting
        }

        return longestStreak
    }

    suspend fun getCompletionRate(habitId: Int, days: Int): Float {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong() - 1).format(dateFormatter)
        val endDate = today.format(dateFormatter)
        val completionCount = habitCompletionDao.getCompletionCount(habitId, startDate, endDate)
        return completionCount.toFloat() / days
    }

    suspend fun getLast5WeeksCompletions(habitId: Int): List<List<Boolean>> {
        val today = LocalDate.now()
        val weeks = mutableListOf<List<Boolean>>()

        for (weekOffset in 4 downTo 0) {
            val weekStart = today.minusWeeks(weekOffset.toLong())
                .with(java.time.DayOfWeek.MONDAY)
            val weekDays = (0L until 7L).map { dayOffset ->
                val date = weekStart.plusDays(dayOffset)
                if (date.isAfter(today)) {
                    false
                } else {
                    val dateStr = date.format(dateFormatter)
                    habitCompletionDao.getCompletion(habitId, dateStr) != null
                }
            }
            weeks.add(weekDays)
        }

        return weeks
    }
}
