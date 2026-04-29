package com.inod.screenofftimer.ui.components.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inod.screenofftimer.viewmodel.TimerViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TimerProgress(viewModel: TimerViewModel) {

    val isRunning = viewModel.isRunning
    val timeLeftSeconds = viewModel.leftSeconds

    val oneHour = 60 * 60

    val baseProgress = (timeLeftSeconds / oneHour.toFloat()).coerceIn(0f, 1f)

    var dragProgress by remember { mutableFloatStateOf(-1f) }

    val displayProgress = if (dragProgress >= 0f) dragProgress else baseProgress

    val sizeDp = 300.dp
    val density = LocalDensity.current

    val strokeWidthDp = 12.dp
    val sizePx = with(density) { sizeDp.toPx() }.toInt()
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }
    val radiusPx = (sizePx - strokeWidthPx) / 2f

    val thumbColor = if (isRunning) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(sizeDp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!isRunning && dragProgress >= 0f) {
                            val totalMinutes = (dragProgress * 60).roundToInt()

                            val finalMinutes = totalMinutes.coerceAtLeast(1)

                            viewModel.setTimer(finalMinutes)
                            dragProgress = -1f
                        }
                    },
                    onDragCancel = {
                        dragProgress = -1f
                    }
                ) { change, _ ->
                    if (isRunning) return@detectDragGestures

                    val rawProgress = calculateProgress(change.position, sizePx)

                    val snappedMinutes = (rawProgress * 60).roundToInt()

                    dragProgress = snappedMinutes / 60f

                    change.consume()
                }
            }
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(sizeDp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )

        CircularProgressIndicator(
            progress = { displayProgress },
            modifier = Modifier.size(sizeDp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )

        // thumb
        Canvas(
            modifier = Modifier.size(sizeDp)
        ) {
            val angleRad = (displayProgress * 2 * Math.PI) - (Math.PI / 2)

            val thumbX = center.x + radiusPx * cos(angleRad).toFloat()
            val thumbY = center.y + radiusPx * sin(angleRad).toFloat()

            drawCircle(
                color = thumbColor,
                radius = strokeWidthPx * 1.5f,
                center = Offset(thumbX, thumbY),
                alpha = if (isRunning) 0.5f else 1f
            )

            drawCircle(
                color = if (isRunning) Color.Transparent else onPrimaryColor,
                radius = strokeWidthPx * 0.5f,
                center = Offset(thumbX, thumbY)
            )
        }

        // text timer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "%02d:%02d".format(
                    (displayProgress * 60).toInt(),
                    ((displayProgress * 3600) % 60).toInt()
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-2).sp
                )
            )
            Text(
                text = "Min : Sec",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

fun calculateProgress(offset: Offset, size: Int): Float {
    val centerX = size / 2f
    val centerY = size / 2f
    val angle = atan2(offset.y - centerY, offset.x - centerX)
    var degrees = Math.toDegrees(angle.toDouble()).toFloat() + 90f
    if (degrees < 0) degrees += 360f
    return degrees / 360f
}