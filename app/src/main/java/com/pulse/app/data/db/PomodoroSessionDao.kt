package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulse.app.data.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSession): Long

    @Query("SELECT * FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND start_time BETWEEN :startMillis AND :endMillis ORDER BY start_time DESC")
    fun getCompletedFocusSessions(startMillis: Long, endMillis: Long): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND start_time BETWEEN :startMillis AND :endMillis")
    fun getCompletedFocusCount(startMillis: Long, endMillis: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(end_time - start_time), 0) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND start_time BETWEEN :startMillis AND :endMillis")
    fun getTotalFocusMillis(startMillis: Long, endMillis: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(end_time - start_time), 0) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1")
    fun getAllTimeFocusMillis(): Flow<Long>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND start_time BETWEEN :startMillis AND :endMillis")
    suspend fun getCompletedFocusCountSync(startMillis: Long, endMillis: Long): Int

    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Int)
}
