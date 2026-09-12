package com.swingcricket.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.swingcricket.game.game.DeliveryData
import com.swingcricket.game.game.Line
import com.swingcricket.game.ui.theme.BallRed
import com.swingcricket.game.ui.theme.PitchGreenDim
import com.swingcricket.game.ui.theme.StadiumNightElevated
import kotlin.math.pow

/** A stylized top-down "the ball is coming at you" visualization driven by [progress] (0f release .. 1f arrival). */
@Composable
fun BallApproach(delivery: DeliveryData?, progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val pitchWidth = size.width * 0.42f
        val pitchLeft = (size.width - pitchWidth) / 2f
        drawRect(
            brush = Brush.verticalGradient(listOf(PitchGreenDim.copy(alpha = 0.35f), StadiumNightElevated.copy(alpha = 0.1f))),
            topLeft = Offset(pitchLeft, 0f),
            size = androidx.compose.ui.geometry.Size(pitchWidth, size.height)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(pitchLeft, size.height * 0.12f),
            end = Offset(pitchLeft + pitchWidth, size.height * 0.12f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(pitchLeft, size.height * 0.92f),
            end = Offset(pitchLeft + pitchWidth, size.height * 0.92f),
            strokeWidth = 6f
        )

        if (delivery != null) {
            val eased = progress.pow(0.85f)
            val offsetFraction = lineOffset(delivery.line)
            val cx = size.width / 2f + offsetFraction * pitchWidth * 0.42f
            val cy = size.height * 0.12f + (size.height * 0.80f) * eased
            val radius = 10f + 34f * eased

            drawCircle(color = BallRed.copy(alpha = 0.25f), radius = radius * 1.8f, center = Offset(cx, cy))
            drawCircle(color = BallRed, radius = radius, center = Offset(cx, cy))
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }
    }
}

private fun lineOffset(line: Line): Float = when (line) {
    Line.WIDE_LEG -> -1f
    Line.LEG_STUMP -> -0.5f
    Line.MIDDLE_STUMP -> 0f
    Line.OFF_STUMP -> 0.5f
    Line.WIDE_OFF -> 1f
}
