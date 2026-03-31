package com.pulse.app.ui.clock

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun ClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        ClockButton(
            isClockedIn = state.isClockedIn,
            onClockIn = viewModel::clockIn,
            onClockOut = viewModel::clockOut
        )

        if (state.isClockedIn && state.currentSessionStartMs != null) {
            Spacer(modifier = Modifier.height(16.dp))
            LiveTimer(startTimeMs = state.currentSessionStartMs!!)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TimesheetCard(
            events = state.weekEvents,
            isExpanded = state.isWeekExpanded,
            onToggle = viewModel::toggleWeekExpanded,
            weekTotalHours = state.weekTotalHours,
            onUpdateEvent = viewModel::updateEvent
        )

        Spacer(modifier = Modifier.height(24.dp))

        ReminderSection(
            reminders = state.reminders,
            onAdd = viewModel::addReminder,
            onDelete = viewModel::deleteReminder,
            onToggle = viewModel::toggleReminder
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ClockButton(
    isClockedIn: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(contentAlignment = Alignment.Center) {
        if (isClockedIn) {
            Box(
                modifier = Modifier
                    .size((120 * pulseScale).dp)
                    .drawBehind {
                        drawCircle(
                            color = accentColor.copy(alpha = pulseAlpha),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
            )
            Button(
                onClick = onClockOut,
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {
                Text(
                    text = "CLOCK OUT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            OutlinedButton(
                onClick = onClockIn,
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, accentColor)
            ) {
                Text(
                    text = "CLOCK IN",
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LiveTimer(startTimeMs: Long) {
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(startTimeMs) {
        while (true) {
            elapsed = System.currentTimeMillis() - startTimeMs
            delay(1000)
        }
    }
    val totalSeconds = elapsed / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    Text(
        text = "%d:%02d:%02d".format(hours, minutes, seconds),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium
    )
}
