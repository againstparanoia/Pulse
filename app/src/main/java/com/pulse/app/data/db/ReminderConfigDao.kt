package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.app.data.model.ReminderConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderConfig): Long

    @Query("SELECT * FROM reminder_configs")
    fun getAllReminders(): Flow<List<ReminderConfig>>

    @Update
    suspend fun update(reminder: ReminderConfig)

    @Query("DELETE FROM reminder_configs WHERE id = :reminderId")
    suspend fun deleteById(reminderId: Int)

    @Query("UPDATE reminder_configs SET is_enabled = :enabled WHERE id = :reminderId")
    suspend fun toggleEnabled(reminderId: Int, enabled: Boolean)
}
