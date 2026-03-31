package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long, // epoch millis

    @ColumnInfo(name = "end_time")
    val endTime: Long, // epoch millis

    @ColumnInfo(name = "type")
    val type: String, // "FOCUS", "SHORT_BREAK", or "LONG_BREAK"

    @ColumnInfo(name = "completed")
    val completed: Boolean = false
)
