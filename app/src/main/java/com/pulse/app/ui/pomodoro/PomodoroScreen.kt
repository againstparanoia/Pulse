package com.pulse.app.ui.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        TimerArc(
            timeRemainingMs = state.timeRemainingMs,
            totalTimeMs = state.totalTimeMs,
            sessionType = state.currentSessionType
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (state.currentSessionType) {
                "FOCUS" -> "Focus"
                "SHORT_BREAK" -> "Short Break"
                "LONG_BREAK" -> "Long Break"
                else -> "Focus"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SessionDots(
            totalSessions = state.sessionsPerCycle,
            completedSessions = state.completedSessions,
            isCurrentRunning = state.isRunning && state.currentSessionType == "FOCUS"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = viewModel::reset,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Reset")
            }

            FilledIconButton(
                onClick = { if (state.isRunning) viewModel.pause() else viewModel.start() },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isRunning) "Pause" else "Play",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            OutlinedIconButton(
                onClick = viewModel::skip,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Outlined.SkipNext, contentDescription = "Skip")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PomodoroSettingsCard(
            settings = state.settings,
            isExpanded = state.isSettingsExpanded,
            onToggle = viewModel::toggleSettings,
            onUpdateSettings = viewModel::updateSettings
        )

        Spacer(modifier = Modifier.height(16.dp))

        PomodoroStatsCard(
            todaySessions = state.todaySessions,
            todayFocusMinutes = state.todayFocusMinutes,
            weekSessions = state.weekSessions,
            allTimeFocusHours = state.allTimeFocusHours,
            dailyStreak = state.dailyStreak,
            last7DaysCounts = state.last7DaysCounts
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
