package com.pulse.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pulse.app.data.db.ClockEventDao
import com.pulse.app.data.db.HabitCompletionDao
import com.pulse.app.data.db.HabitDao
import com.pulse.app.data.db.PomodoroSessionDao
import com.pulse.app.data.db.PomodoroSettingsDao
import com.pulse.app.data.db.PulseDatabase
import com.pulse.app.data.db.ReminderConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePulseDatabase(
        @ApplicationContext context: Context
    ): PulseDatabase {
        return Room.databaseBuilder(
            context,
            PulseDatabase::class.java,
            "pulse_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO pomodoro_settings (id, focus_minutes, short_break_minutes, long_break_minutes, sessions_per_cycle)
                        VALUES (1, 25, 5, 15, 4)
                        """.trimIndent()
                    )
                }
            })
            .build()
    }

    @Provides
    fun provideClockEventDao(database: PulseDatabase): ClockEventDao {
        return database.clockEventDao()
    }

    @Provides
    fun provideHabitDao(database: PulseDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitCompletionDao(database: PulseDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }

    @Provides
    fun providePomodoroSessionDao(database: PulseDatabase): PomodoroSessionDao {
        return database.pomodoroSessionDao()
    }

    @Provides
    fun providePomodoroSettingsDao(database: PulseDatabase): PomodoroSettingsDao {
        return database.pomodoroSettingsDao()
    }

    @Provides
    fun provideReminderConfigDao(database: PulseDatabase): ReminderConfigDao {
        return database.reminderConfigDao()
    }
}
