package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulse.app.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: HabitCompletion): Long

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND completed_date BETWEEN :startDate AND :endDate ORDER BY completed_date ASC")
    fun getCompletions(habitId: Int, startDate: String, endDate: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId ORDER BY completed_date DESC")
    fun getAllCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND completed_date = :date LIMIT 1")
    suspend fun getCompletion(habitId: Int, date: String): HabitCompletion?

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId AND completed_date = :date")
    suspend fun delete(habitId: Int, date: String)

    @Query("SELECT completed_date FROM habit_completions WHERE habit_id = :habitId ORDER BY completed_date DESC")
    suspend fun getAllCompletionDates(habitId: Int): List<String>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habit_id = :habitId AND completed_date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionCount(habitId: Int, startDate: String, endDate: String): Int
}
