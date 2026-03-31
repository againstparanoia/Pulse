package com.pulse.app.data.repository

import com.pulse.app.data.db.ClockEventDao
import com.pulse.app.data.db.ReminderConfigDao
import com.pulse.app.data.model.ClockEvent
import com.pulse.app.data.model.ReminderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClockRepository @Inject constructor(
    private val clockEventDao: ClockEventDao,
    private val reminderConfigDao: ReminderConfigDao
) {

    fun isClockedIn(): Flow<Boolean> =
        clockEventDao.getAllEventsOrdered().map { events ->
            val latest = events.firstOrNull()
            latest?.type == "CLOCK_IN"
        }

    fun getLatestClockIn(): Flow<ClockEvent?> =
        clockEventDao.getAllEventsOrdered().map { events ->
            events.firstOrNull { it.type == "CLOCK_IN" }
        }

    fun getWeekEvents(startMillis: Long, endMillis: Long): Flow<List<ClockEvent>> =
        clockEventDao.getEventsBetween(startMillis, endMillis)

    fun getEventsBetween(startMillis: Long, endMillis: Long): Flow<List<ClockEvent>> =
        clockEventDao.getEventsBetween(startMillis, endMillis)

    suspend fun getLatestEvent(): ClockEvent? =
        clockEventDao.getLatestEvent()

    suspend fun clockIn() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        clockEventDao.insert(
            ClockEvent(
                type = "CLOCK_IN",
                timestamp = now.toEpochMilli(),
                timezone = zone.id,
                isManualEdit = false
            )
        )
    }

    suspend fun clockOut() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        clockEventDao.insert(
            ClockEvent(
                type = "CLOCK_OUT",
                timestamp = now.toEpochMilli(),
                timezone = zone.id,
                isManualEdit = false
            )
        )
    }

    suspend fun insertEvent(event: ClockEvent): Long =
        clockEventDao.insert(event)

    suspend fun updateEvent(event: ClockEvent) =
        clockEventDao.update(event)

    suspend fun deleteEvent(eventId: Int) =
        clockEventDao.deleteById(eventId)

    // Reminders

    fun getAllReminders(): Flow<List<ReminderConfig>> =
        reminderConfigDao.getAllReminders()

    suspend fun addReminder(reminder: ReminderConfig): Long =
        reminderConfigDao.insert(reminder)

    suspend fun insertReminder(reminder: ReminderConfig): Long =
        reminderConfigDao.insert(reminder)

    suspend fun updateReminder(reminder: ReminderConfig) =
        reminderConfigDao.update(reminder)

    suspend fun deleteReminder(reminderId: Int) =
        reminderConfigDao.deleteById(reminderId)

    suspend fun toggleReminder(reminderId: Int, enabled: Boolean) =
        reminderConfigDao.toggleEnabled(reminderId, enabled)
}
