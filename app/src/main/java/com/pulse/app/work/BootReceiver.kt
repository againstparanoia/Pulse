package com.pulse.app.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pulse.app.data.db.PulseDatabase
import com.pulse.app.data.model.Habit
import com.pulse.app.data.model.ReminderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                ).build()

                val workManager = WorkManager.getInstance(context.applicationContext)

                rescheduleClockReminders(database, workManager)
                rescheduleHabitReminders(database, workManager)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleClockReminders(
        database: PulseDatabase,
        workManager: WorkManager
    ) {
        val reminders: List<ReminderConfig> = database.reminderConfigDao()
            .getAllReminders()
            .first()

        for (reminder in reminders) {
            if (!reminder.isEnabled) continue

            val initialDelay = calculateInitialDelay(reminder.hour, reminder.minute)

            val inputData = Data.Builder()
                .putString(ClockReminderWorker.KEY_LABEL, reminder.label)
                .putInt(ClockReminderWorker.KEY_REMINDER_ID, reminder.id)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ClockReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "clock_reminder_${reminder.id}",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    private suspend fun rescheduleHabitReminders(
        database: PulseDatabase,
        workManager: WorkManager
    ) {
        val habits: List<Habit> = database.habitDao()
            .getAllActiveHabits()
            .first()

        for (habit in habits) {
            if (!habit.notificationEnabled) continue

            val notificationTimes = parseNotificationTimes(habit.notificationTimes)
            for ((index, time) in notificationTimes.withIndex()) {
                val initialDelay = calculateInitialDelay(time.hour, time.minute)

                val inputData = Data.Builder()
                    .putInt(HabitReminderWorker.KEY_HABIT_ID, habit.id)
                    .putString(HabitReminderWorker.KEY_HABIT_NAME, habit.name)
                    .putString(HabitReminderWorker.KEY_HABIT_EMOJI, habit.emoji)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    24, TimeUnit.HOURS
                )
                    .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "habit_reminder_${habit.id}_$index",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var targetTime = now.toLocalDate().atTime(hour, minute)
        if (targetTime.isBefore(now) || targetTime.isEqual(now)) {
            targetTime = targetTime.plusDays(1)
        }
        return Duration.between(now, targetTime)
    }

    private fun parseNotificationTimes(json: String): List<LocalTime> {
        val times = mutableListOf<LocalTime>()
        val cleaned = json.trim().removePrefix("[").removeSuffix("]")
        if (cleaned.isBlank()) return times

        val entries = cleaned.split(",")
        for (entry in entries) {
            val timeStr = entry.trim().removeSurrounding("\"")
            if (timeStr.isNotBlank()) {
                try {
                    times.add(LocalTime.parse(timeStr))
                } catch (_: Exception) {
                    // Skip malformed time entries
                }
            }
        }
        return times
    }
}
