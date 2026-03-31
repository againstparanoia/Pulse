package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_settings")
data class PomodoroSettings(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "focus_minutes")
    val focusMinutes: Int = 25,

    @ColumnInfo(name = "short_break_minutes")
    val shortBreakMinutes: Int = 5,

    @ColumnInfo(name = "long_break_minutes")
    val longBreakMinutes: Int = 15,

    @ColumnInfo(name = "sessions_per_cycle")
    val sessionsPerCycle: Int = 4
) {
    companion object {
        fun default(): PomodoroSettings = PomodoroSettings()
    }
}
