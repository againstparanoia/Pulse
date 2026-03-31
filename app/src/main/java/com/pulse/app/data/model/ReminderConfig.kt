package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_configs")
data class ReminderConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "hour")
    val hour: Int, // 0-23

    @ColumnInfo(name = "minute")
    val minute: Int, // 0-59

    @ColumnInfo(name = "active_days")
    val activeDays: String, // comma-separated e.g. "MON,TUE,WED"

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true
)
