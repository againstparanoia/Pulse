package com.pulse.app.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ClockReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "clock_reminders"
        const val CHANNEL_NAME = "Clock Reminders"
        const val KEY_LABEL = "label"
        const val KEY_REMINDER_ID = "reminder_id"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        val label = inputData.getString(KEY_LABEL) ?: "Work"
        val reminderId = inputData.getInt(KEY_REMINDER_ID, 0)
        val notificationId = NOTIFICATION_ID_BASE + reminderId

        createNotificationChannel()

        val clockInIntent = Intent(context, ClockActionReceiver::class.java).apply {
            action = ClockActionReceiver.ACTION_CLOCK_IN
            putExtra(ClockActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val clockInPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            clockInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val clockOutIntent = Intent(context, ClockActionReceiver::class.java).apply {
            action = ClockActionReceiver.ACTION_CLOCK_OUT
            putExtra(ClockActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val clockOutPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            clockOutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Clock Reminder")
            .setContentText("$label \u2014 Time to clock in!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_play,
                "Clock In",
                clockInPendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "Clock Out",
                clockOutPendingIntent
            )
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for clock-in and clock-out reminders"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
