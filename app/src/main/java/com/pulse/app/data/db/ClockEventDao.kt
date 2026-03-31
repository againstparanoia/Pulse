package com.pulse.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.app.data.model.ClockEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ClockEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ClockEvent): Long

    @Query("SELECT * FROM clock_events ORDER BY timestamp DESC")
    fun getAllEventsOrdered(): Flow<List<ClockEvent>>

    @Query("SELECT * FROM clock_events WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp DESC")
    fun getEventsBetween(startMillis: Long, endMillis: Long): Flow<List<ClockEvent>>

    @Update
    suspend fun update(event: ClockEvent)

    @Query("SELECT * FROM clock_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEvent(): ClockEvent?

    @Query("DELETE FROM clock_events WHERE id = :eventId")
    suspend fun deleteById(eventId: Int)
}
