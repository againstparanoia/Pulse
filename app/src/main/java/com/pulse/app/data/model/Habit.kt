package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "emoji")
    val emoji: String,

    @ColumnInfo(name = "frequency")
    val frequency: String, // "DAILY", "X_PER_WEEK", or "SPECIFIC_DAYS"

    @ColumnInfo(name = "frequency_count")
    val frequencyCount: Int = 1,

    @ColumnInfo(name = "active_days")
    val activeDays: String = "", // comma-separated e.g. "MON,WED,FRI"

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = false,

    @ColumnInfo(name = "notification_times")
    val notificationTimes: String = "[]", // JSON array of time strings

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)
