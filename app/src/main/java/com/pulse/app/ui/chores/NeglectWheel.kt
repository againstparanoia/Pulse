package com.pulse.app.ui.chores

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val WHEEL_COLORS = listOf(
    Color(0xFFD97757), Color(0xFFE8A882), Color(0xFFA67C6D), Color(0xFFBF9B8A),
    Color(0xFFD4A574), Color(0xFF8B6355), Color(0xFFCB8B6E), Color(0xFF9E7060)
)

@Composable
fun NeglectWheel(
    chores: List<ChoreWithWeight>,
    isSpinning: Boolean,
    targetChore: ChoreWithWeight?,
    onSpinFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(isSpinning, targetChore?.chore?.id) {
        if (isSpinning && targetChore != null && chores.isNotEmpty()) {
            val targetIndex = chores.indexOfFirst { it.chore.id == targetChore.chore.id }
            if (targetIndex < 0) {
                onSpinFinished()
                return@LaunchedEffect
            }

            var degreesToCenter = 0f
            for (i in 0 until targetIndex) {
                degreesToCenter += chores[i].normalizedWeight * 360f
            }
            degreesToCenter += chores[targetIndex].normalizedWeight * 360f / 2f

            val landingOffset = (360f - degreesToCenter % 360f) % 360f
            val delta = (landingOffset - rotation.value % 360f + 360f) % 360f
            val totalTarget = rotation.value + delta + 360f * 5f

            rotation.animateTo(
                targetValue = totalTarget,
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )
            onSpinFinished()
        }
    }

    Canvas(modifier = modifier.size(280.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width / 2f - 8.dp.toPx()

        if (chores.isEmpty()) {
            drawCircle(color = Color(0xFFD97757), radius = radius, center = Offset(cx, cy))
        } else {
            rotate(rotation.value % 360f, pivot = Offset(cx, cy)) {
                var startAngle = -90f
                chores.forEachIndexed { index, cw ->
                    val sweep = cw.normalizedWeight * 360f
                    drawArc(
                        color = WHEEL_COLORS[index % WHEEL_COLORS.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2f, radius * 2f)
                    )
                    if (sweep >= 18f) {
                        val midAngleRad = Math.toRadians((startAngle + sweep / 2f).toDouble())
                        val textR = radius * 0.62f
                        val tx = cx + textR * cos(midAngleRad).toFloat()
                        val ty = cy + textR * sin(midAngleRad).toFloat()
                        val measured = textMeasurer.measure(
                            text = cw.chore.emoji,
                            style = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center)
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                tx - measured.size.width / 2f,
                                ty - measured.size.height / 2f
                            )
                        )
                    }
                    val lineRad = Math.toRadians(startAngle.toDouble())
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(cx, cy),
                        end = Offset(
                            cx + radius * cos(lineRad).toFloat(),
                            cy + radius * sin(lineRad).toFloat()
                        ),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    startAngle += sweep
                }
            }
        }

        // Center cap — not rotating
        drawCircle(color = Color.White, radius = 22.dp.toPx(), center = Offset(cx, cy))
        drawCircle(color = Color(0xFFD97757), radius = 9.dp.toPx(), center = Offset(cx, cy))

        // Fixed pointer triangle at top
        val ptrSize = 14.dp.toPx()
        val ptrTip = cy - radius - 2.dp.toPx()
        val pointerPath = Path().apply {
            moveTo(cx, ptrTip - ptrSize)
            lineTo(cx - ptrSize / 2f, ptrTip + ptrSize / 2f)
            lineTo(cx + ptrSize / 2f, ptrTip + ptrSize / 2f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFF5C3A2E))
    }
}
