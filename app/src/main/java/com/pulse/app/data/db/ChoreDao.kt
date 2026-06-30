package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.app.data.model.Chore
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chore: Chore): Long

    @Update
    suspend fun update(chore: Chore)

    @Query("SELECT * FROM chores WHERE is_archived = 0 ORDER BY name ASC")
    fun getActiveChores(): Flow<List<Chore>>

    @Query("SELECT * FROM chores WHERE id = :id")
    suspend fun getById(id: Int): Chore?

    @Query("DELETE FROM chores WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE chores SET is_archived = 1 WHERE id = :id")
    suspend fun archive(id: Int)

    @Query("UPDATE chores SET last_completed_at = :completedAt, completed_count = completed_count + 1 WHERE id = :id")
    suspend fun markCompleted(id: Int, completedAt: Long)
}
