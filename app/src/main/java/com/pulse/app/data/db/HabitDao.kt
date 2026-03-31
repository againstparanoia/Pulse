package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.app.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Query("SELECT * FROM habits WHERE is_archived = 0 ORDER BY created_at ASC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE is_archived = 1 ORDER BY created_at ASC")
    fun getArchivedHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getById(habitId: Int): Habit?

    @Update
    suspend fun update(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteById(habitId: Int)

    @Query("UPDATE habits SET is_archived = :archived WHERE id = :habitId")
    suspend fun setArchived(habitId: Int, archived: Boolean)
}
