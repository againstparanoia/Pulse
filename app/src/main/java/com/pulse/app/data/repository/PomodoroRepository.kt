package com.pulse.app.data.repository

import com.pulse.app.data.db.PomodoroSessionDao
import com.pulse.app.data.db.PomodoroSettingsDao
import com.pulse.app.data.model.PomodoroSession
import com.pulse.app.data.model.PomodoroSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val sessionDao: PomodoroSessionDao,
    private val settingsDao: PomodoroSettingsDao
) {

    fun getSettings(): Flow<PomodoroSettings?> =
        settingsDao.getSettings()

    suspend fun updateSettings(settings: PomodoroSettings) =
        settingsDao.insert(settings)

    suspend fun saveSettings(settings: PomodoroSettings) =
        settingsDao.insert(settings)

    suspend fun saveSession(session: PomodoroSession): Long =
        sessionDao.insert(session)

    suspend fun insertSession(session: PomodoroSession): Long =
        sessionDao.insert(session)

    fun getTodayStats(): Flow<Pair<Int, Int>> {
        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val todayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return combine(
            sessionDao.getCompletedFocusCount(todayStart, todayEnd),
            sessionDao.getTotalFocusMillis(todayStart, todayEnd)
        ) { count, millis ->
            Pair(count, (millis / 60000).toInt())
        }
    }

    fun getWeekSessionCount(): Flow<Int> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEnd = today.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return sessionDao.getCompletedFocusCount(weekStart, weekEnd)
    }

    fun getAllTimeFocusHours(): Flow<Float> =
        sessionDao.getAllTimeFocusMillis().map { millis ->
            millis / 3_600_000f
        }

    suspend fun getDailyStreak(): Int {
        val zone = ZoneId.systemDefault()
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = date.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            val count = sessionDao.getCompletedFocusCountSync(dayStart, dayEnd)
            if (count > 0) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    fun getLast7DaysCounts(): Flow<List<Int>> {
        val zone = ZoneId.systemDefault()
        val flows = (6 downTo 0).map { daysAgo ->
            val date = LocalDate.now().minusDays(daysAgo.toLong())
            val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = date.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            sessionDao.getCompletedFocusCount(dayStart, dayEnd)
        }
        return combine(flows) { it.toList() }
    }

    fun getCompletedFocusSessions(startMillis: Long, endMillis: Long): Flow<List<PomodoroSession>> =
        sessionDao.getCompletedFocusSessions(startMillis, endMillis)

    fun getCompletedFocusCount(startMillis: Long, endMillis: Long): Flow<Int> =
        sessionDao.getCompletedFocusCount(startMillis, endMillis)

    fun getTotalFocusMillis(startMillis: Long, endMillis: Long): Flow<Long> =
        sessionDao.getTotalFocusMillis(startMillis, endMillis)

    fun getAllTimeFocusMillis(): Flow<Long> =
        sessionDao.getAllTimeFocusMillis()

    suspend fun getCompletedFocusCountSync(startMillis: Long, endMillis: Long): Int =
        sessionDao.getCompletedFocusCountSync(startMillis, endMillis)
}
