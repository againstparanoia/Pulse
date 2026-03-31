package com.pulse.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pulse.app.data.model.ClockEvent
import com.pulse.app.data.model.Habit
import com.pulse.app.data.model.HabitCompletion
import com.pulse.app.data.model.PomodoroSession
import com.pulse.app.data.model.PomodoroSettings
import com.pulse.app.data.model.ReminderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClockEvent::class,
        ReminderConfig::class,
        PomodoroSession::class,
        PomodoroSettings::class,
        Habit::class,
        HabitCompletion::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PulseDatabase : RoomDatabase() {

    abstract fun clockEventDao(): ClockEventDao
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun pomodoroSettingsDao(): PomodoroSettingsDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        fun getDatabase(context: Context): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrepopulateCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.pomodoroSettingsDao().insert(
                            PomodoroSettings(
                                id = 1,
                                focusMinutes = 25,
                                shortBreakMinutes = 5,
                                longBreakMinutes = 15,
                                sessionsPerCycle = 4
                            )
                        )
                    }
                }
            }
        }
    }
}
