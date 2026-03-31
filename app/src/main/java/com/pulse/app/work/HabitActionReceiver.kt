package com.pulse.app.work

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.pulse.app.data.db.PulseDatabase
import com.pulse.app.data.model.HabitCompletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_DONE = "com.pulse.app.ACTION_MARK_DONE"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_DONE) return

        val habitId = intent.getIntExtra(EXTRA_HABIT_ID, 0)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                ).build()

                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                val existing = database.habitCompletionDao().getCompletion(habitId, today)
                if (existing == null) {
                    val completion = HabitCompletion(
                        habitId = habitId,
                        completedDate = today
                    )
                    database.habitCompletionDao().insert(completion)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
