package com.pulse.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pulse.app.data.db.ChoreDao
import com.pulse.app.data.db.ClockEventDao
import com.pulse.app.data.db.HabitCompletionDao
import com.pulse.app.data.db.HabitDao
import com.pulse.app.data.db.MIGRATION_1_2
import com.pulse.app.data.db.PomodoroSessionDao
import com.pulse.app.data.db.PomodoroSettingsDao
import com.pulse.app.data.db.PulseDatabase
import com.pulse.app.data.db.ReminderConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .addMigrations(MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO pomodoro_settings (id, focus_minutes, short_break_minutes, long_break_minutes, sessions_per_cycle)
                        VALUES (1, 25, 5, 15, 4)
                        """.trimIndent()
                    )
                    listOf(
                        Triple("Wash dishes", "🍽️", 1),
                        Triple("Wipe counters", "🧽", 2),
                        Triple("Sweep / vacuum", "🧹", 3),
                        Triple("Take out trash", "🗑️", 3),
                        Triple("Water plants", "🌿", 3),
                        Triple("Do laundry", "🧺", 7),
                        Triple("Clean toilet", "🚽", 7),
                        Triple("Clean bathroom sink", "🪥", 7),
                        Triple("Mop floors", "🪧", 7),
                        Triple("Clean shower", "🚿", 14),
                        Triple("Change bed sheets", "🛏️", 14),
                        Triple("Wipe mirrors", "🪟", 14),
                        Triple("Dust surfaces", "🪣", 14),
                        Triple("Clean fridge", "❄️", 30),
                        Triple("Organize pantry", "📦", 30)
                    ).forEach { (name, emoji, freq) ->
                        db.execSQL(
                            "INSERT OR IGNORE INTO chores (name, emoji, expected_frequency_days) VALUES (?, ?, ?)",
                            arrayOf(name, emoji, freq)
                        )
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideClockEventDao(database: PulseDatabase): ClockEventDao = database.clockEventDao()

    @Provides
    fun provideHabitDao(database: PulseDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitCompletionDao(database: PulseDatabase): HabitCompletionDao = database.habitCompletionDao()

    @Provides
    fun providePomodoroSessionDao(database: PulseDatabase): PomodoroSessionDao = database.pomodoroSessionDao()

    @Provides
    fun providePomodoroSettingsDao(database: PulseDatabase): PomodoroSettingsDao = database.pomodoroSettingsDao()

    @Provides
    fun provideReminderConfigDao(database: PulseDatabase): ReminderConfigDao = database.reminderConfigDao()

    @Provides
    fun provideChoreDao(database: PulseDatabase): ChoreDao = database.choreDao()
}
