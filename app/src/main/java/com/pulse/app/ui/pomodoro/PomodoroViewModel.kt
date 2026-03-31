package com.pulse.app.ui.pomodoro

import android.app.Application
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.app.R
import com.pulse.app.data.model.PomodoroSession
import com.pulse.app.data.model.PomodoroSettings
import com.pulse.app.data.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class PomodoroUiState(
    val timeRemainingMs: Long = 25 * 60 * 1000L,
    val totalTimeMs: Long = 25 * 60 * 1000L,
    val isRunning: Boolean = false,
    val currentSessionType: String = "FOCUS",
    val completedSessions: Int = 0,
    val sessionsPerCycle: Int = 4,
    val settings: PomodoroSettings = PomodoroSettings.default(),
    val todaySessions: Int = 0,
    val todayFocusMinutes: Int = 0,
    val weekSessions: Int = 0,
    val allTimeFocusHours: Float = 0f,
    val dailyStreak: Int = 0,
    val last7DaysCounts: List<Int> = List(7) { 0 },
    val isSettingsExpanded: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val repository: PomodoroRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L

    init {
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                val s = settings ?: PomodoroSettings.default()
                _uiState.update { state ->
                    if (!state.isRunning) {
                        val totalMs = getSessionDurationMs(state.currentSessionType, s)
                        state.copy(
                            settings = s,
                            sessionsPerCycle = s.sessionsPerCycle,
                            totalTimeMs = totalMs,
                            timeRemainingMs = totalMs,
                            isLoading = false
                        )
                    } else {
                        state.copy(settings = s, sessionsPerCycle = s.sessionsPerCycle, isLoading = false)
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.getTodayStats().collect { (count, minutes) ->
                _uiState.update { it.copy(todaySessions = count, todayFocusMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            repository.getWeekSessionCount().collect { count ->
                _uiState.update { it.copy(weekSessions = count) }
            }
        }
        viewModelScope.launch {
            repository.getAllTimeFocusHours().collect { hours ->
                _uiState.update { it.copy(allTimeFocusHours = hours) }
            }
        }
        viewModelScope.launch {
            val streak = repository.getDailyStreak()
            _uiState.update { it.copy(dailyStreak = streak) }
        }
        viewModelScope.launch {
            repository.getLast7DaysCounts().collect { counts ->
                _uiState.update { it.copy(last7DaysCounts = counts) }
            }
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        sessionStartTime = System.currentTimeMillis()
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingMs > 0 && _uiState.value.isRunning) {
                delay(100)
                _uiState.update { state ->
                    val remaining = (state.timeRemainingMs - 100).coerceAtLeast(0)
                    state.copy(timeRemainingMs = remaining)
                }
            }
            if (_uiState.value.timeRemainingMs <= 0) {
                onSessionComplete()
            }
        }
    }

    fun pause() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.update { state ->
            val totalMs = getSessionDurationMs(state.currentSessionType, state.settings)
            state.copy(isRunning = false, timeRemainingMs = totalMs, totalTimeMs = totalMs)
        }
    }

    fun skip() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
        advanceToNextSession()
    }

    fun updateSettings(settings: PomodoroSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            timerJob?.cancel()
            _uiState.update { state ->
                val totalMs = getSessionDurationMs(state.currentSessionType, settings)
                state.copy(
                    isRunning = false,
                    settings = settings,
                    sessionsPerCycle = settings.sessionsPerCycle,
                    totalTimeMs = totalMs,
                    timeRemainingMs = totalMs
                )
            }
        }
    }

    fun toggleSettings() {
        _uiState.update { it.copy(isSettingsExpanded = !it.isSettingsExpanded) }
    }

    private suspend fun onSessionComplete() {
        val state = _uiState.value
        val endTime = System.currentTimeMillis()
        val session = PomodoroSession(
            startTime = sessionStartTime,
            endTime = endTime,
            type = state.currentSessionType,
            completed = true
        )
        repository.saveSession(session)

        playChime()
        vibrate()

        advanceToNextSession()
    }

    private fun advanceToNextSession() {
        _uiState.update { state ->
            val nextType: String
            val newCompleted: Int
            when (state.currentSessionType) {
                "FOCUS" -> {
                    newCompleted = state.completedSessions + 1
                    nextType = if (newCompleted % state.sessionsPerCycle == 0) "LONG_BREAK" else "SHORT_BREAK"
                }
                "SHORT_BREAK", "LONG_BREAK" -> {
                    newCompleted = state.completedSessions
                    nextType = "FOCUS"
                }
                else -> {
                    newCompleted = state.completedSessions
                    nextType = "FOCUS"
                }
            }
            val totalMs = getSessionDurationMs(nextType, state.settings)
            state.copy(
                isRunning = false,
                currentSessionType = nextType,
                completedSessions = newCompleted,
                totalTimeMs = totalMs,
                timeRemainingMs = totalMs
            )
        }
    }

    private fun getSessionDurationMs(type: String, settings: PomodoroSettings): Long {
        return when (type) {
            "FOCUS" -> settings.focusMinutes * 60 * 1000L
            "SHORT_BREAK" -> settings.shortBreakMinutes * 60 * 1000L
            "LONG_BREAK" -> settings.longBreakMinutes * 60 * 1000L
            else -> settings.focusMinutes * 60 * 1000L
        }
    }

    private fun playChime() {
        try {
            val context = getApplication<Application>()
            val mp = MediaPlayer.create(context, R.raw.chime)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (_: Exception) {
            // Device may not support audio
        }
    }

    private fun vibrate() {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
            // Device may not support vibration
        }
    }
}
