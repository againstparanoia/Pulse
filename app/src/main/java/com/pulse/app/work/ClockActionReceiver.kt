package com.pulse.app.work

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.pulse.app.data.db.PulseDatabase
import com.pulse.app.data.model.ClockEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class ClockActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CLOCK_IN = "com.pulse.app.ACTION_CLOCK_IN"
        const val ACTION_CLOCK_OUT = "com.pulse.app.ACTION_CLOCK_OUT"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        val eventType = when (intent.action) {
            ACTION_CLOCK_IN -> "CLOCK_IN"
            ACTION_CLOCK_OUT -> "CLOCK_OUT"
            else -> return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                ).build()

                val now = Instant.now()
                val zoneId = ZoneId.systemDefault()

                val clockEvent = ClockEvent(
                    type = eventType,
                    timestamp = now.toEpochMilli(),
                    timezone = zoneId.id,
                    isManualEdit = false
                )

                database.clockEventDao().insert(clockEvent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
