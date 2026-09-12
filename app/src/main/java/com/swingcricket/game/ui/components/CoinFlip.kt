package com.swingcricket.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.StadiumNight
import com.swingcricket.game.ui.theme.StadiumOutline

/**
 * A spinning coin that settles on HEADS (host won) or TAILS (guest won) once [flipping]
 * turns false. While flipping, it spins continuously for a lively "still deciding" feel.
 */
@Composable
fun CoinFlip(flipping: Boolean, hostWon: Boolean?, modifier: Modifier = Modifier) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(flipping) {
        while (flipping) {
            rotation.animateTo(rotation.value + 360f, tween(durationMillis = 200, easing = LinearEasing))
        }
    }

    LaunchedEffect(flipping, hostWon) {
        if (!flipping && hostWon != null) {
            val targetExtra = if (hostWon) 0f else 180f
            val base = kotlin.math.ceil(rotation.value / 360f) * 360f
            rotation.animateTo(base + targetExtra, tween(400, easing = LinearEasing))
        }
    }

    Box(
        modifier = modifier
            .size(120.dp)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 24f
            }
            .background(Brush.radialGradient(listOf(FloodlightGold, StadiumOutline)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🏏",
            style = MaterialTheme.typography.headlineLarge,
            color = StadiumNight,
            fontWeight = FontWeight.Black
        )
    }
}
