package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.app.data.model.PomodoroSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: PomodoroSettings)

    @Query("SELECT * FROM pomodoro_settings WHERE id = 1")
    fun getSettings(): Flow<PomodoroSettings?>

    @Update
    suspend fun update(settings: PomodoroSettings)
}
