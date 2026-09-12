package com.swingcricket.game.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.theme.PitchGreen

@Composable
fun PulsingRadar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "radar")
    val ring1 by ringProgress(transition, 0)
    val ring2 by ringProgress(transition, 700)
    val ring3 by ringProgress(transition, 1400)

    Box(modifier = modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            listOf(ring1, ring2, ring3).forEach { progress ->
                drawCircle(
                    color = PitchGreen.copy(alpha = (1f - progress) * 0.5f),
                    radius = size.minDimension / 2f * progress
                )
            }
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(PitchGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color.Black)
        }
    }
}

@Composable
private fun ringProgress(transition: InfiniteTransition, delayMillis: Int) =
    transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
