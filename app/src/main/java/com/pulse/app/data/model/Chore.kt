package com.pulse.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "emoji") val emoji: String,
    @ColumnInfo(name = "expected_frequency_days") val expectedFrequencyDays: Int,
    @ColumnInfo(name = "last_completed_at") val lastCompletedAt: Long? = null,
    @ColumnInfo(name = "completed_count") val completedCount: Int = 0,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false
)
