package com.pulse.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pulse.app.data.model.Chore
import com.pulse.app.data.model.ClockEvent
import com.pulse.app.data.model.Habit
import com.pulse.app.data.model.HabitCompletion
import com.pulse.app.data.model.PomodoroSession
import com.pulse.app.data.model.PomodoroSettings
import com.pulse.app.data.model.ReminderConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chores (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                emoji TEXT NOT NULL,
                expected_frequency_days INTEGER NOT NULL,
                last_completed_at INTEGER,
                completed_count INTEGER NOT NULL DEFAULT 0,
                is_archived INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [
        ClockEvent::class,
        ReminderConfig::class,
        PomodoroSession::class,
        PomodoroSettings::class,
        Habit::class,
        HabitCompletion::class,
        Chore::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PulseDatabase : RoomDatabase() {

    abstract fun clockEventDao(): ClockEventDao
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun pomodoroSettingsDao(): PomodoroSettingsDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun choreDao(): ChoreDao

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
                    .addMigrations(MIGRATION_1_2)
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
