package com.swingcricket.game.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.swingcricket.game.sensors.SwingPhase
import com.swingcricket.game.ui.theme.BallRed
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.StadiumNightElevated
import com.swingcricket.game.ui.theme.TextSecondary
import com.swingcricket.game.ui.theme.Motion

@Composable
fun SwingMeter(phase: SwingPhase, intensity: Float, label: String, modifier: Modifier = Modifier) {
    val animatedIntensity by animateFloatAsState(intensity, Motion.snappy(), label = "intensity")

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = phaseLabel(phase, label),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val corner = CornerRadius(size.height / 2, size.height / 2)
                drawRoundRect(color = StadiumNightElevated, cornerRadius = corner)
                clipRect(right = size.width * animatedIntensity) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(PitchGreen, FloodlightGold, BallRed)),
                        cornerRadius = corner,
                        size = Size(size.width, size.height)
                    )
                }
            }
        }
    }
}

private fun phaseLabel(phase: SwingPhase, base: String): String = when (phase) {
    SwingPhase.IDLE -> base
    SwingPhase.LOADING -> "Loading up…"
    SwingPhase.SWINGING -> "Swinging!"
    SwingPhase.IMPACT -> "Impact!"
    SwingPhase.RECOVERING -> "Resetting…"
}
