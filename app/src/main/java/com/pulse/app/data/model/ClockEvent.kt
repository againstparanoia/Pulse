package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clock_events")
data class ClockEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "type")
    val type: String, // "CLOCK_IN" or "CLOCK_OUT"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long, // epoch millis

    @ColumnInfo(name = "timezone")
    val timezone: String, // ZoneId string e.g. "America/New_York"

    @ColumnInfo(name = "is_manual_edit")
    val isManualEdit: Boolean = false
)
