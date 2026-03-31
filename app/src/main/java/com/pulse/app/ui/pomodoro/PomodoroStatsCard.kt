package com.pulse.app.ui.pomodoro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.app.ui.components.StatChip

@Composable
fun PomodoroStatsCard(
    todaySessions: Int,
    todayFocusMinutes: Int,
    weekSessions: Int,
    allTimeFocusHours: Float,
    dailyStreak: Int,
    last7DaysCounts: List<Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Today",
                    value = "$todaySessions sessions · ${todayFocusMinutes}m",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "This Week",
                    value = "$weekSessions sessions",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "All Time",
                    value = "%.1f hrs".format(allTimeFocusHours),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Streak",
                    value = "$dailyStreak days",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SparkLine(data = last7DaysCounts)
        }
    }
}

@Composable
private fun SparkLine(data: List<Int>) {
    val accentColor = MaterialTheme.colorScheme.primary
    val fillColor = accentColor.copy(alpha = 0.15f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (data.isEmpty() || data.all { it == 0 }) return@Canvas

        val maxVal = data.max().coerceAtLeast(1).toFloat()
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = size.height - (value / maxVal * size.height)
            )
        }

        val fillPath = Path().apply {
            moveTo(0f, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(fillPath, fillColor)

        for (i in 0 until points.size - 1) {
            drawLine(
                color = accentColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx()
            )
        }

        points.forEach { point ->
            drawCircle(
                color = accentColor,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}
