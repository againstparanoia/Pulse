package com.pulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PulseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val clockChannel = NotificationChannel(
            CHANNEL_CLOCK,
            "Clock Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to clock in or out"
        }

        val pomodoroChannel = NotificationChannel(
            CHANNEL_POMODORO,
            "Pomodoro Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pomodoro session completion alerts"
        }

        val habitChannel = NotificationChannel(
            CHANNEL_HABITS,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily habit reminders"
        }

        manager.createNotificationChannel(clockChannel)
        manager.createNotificationChannel(pomodoroChannel)
        manager.createNotificationChannel(habitChannel)
    }

    companion object {
        const val CHANNEL_CLOCK = "clock_reminders"
        const val CHANNEL_POMODORO = "pomodoro_timer"
        const val CHANNEL_HABITS = "habit_reminders"
    }
}
