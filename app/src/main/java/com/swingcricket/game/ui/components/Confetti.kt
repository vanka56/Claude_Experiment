package com.swingcricket.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import com.swingcricket.game.ui.theme.BallRed
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.SkyBlue
import kotlin.random.Random

private data class ConfettiPiece(
    val startX: Float,
    val fallSpeed: Float,
    val drift: Float,
    val size: Float,
    val rotationSpeed: Float,
    val colorIndex: Int,
)

/** Lightweight falling-confetti celebration, driven by a single animated clock float — no per-frame allocation. */
@Composable
fun Confetti(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return
    val colors = listOf(PitchGreen, FloodlightGold, BallRed, SkyBlue)
    val pieces = remember {
        List(60) {
            ConfettiPiece(
                startX = Random.nextFloat(),
                fallSpeed = 0.18f + Random.nextFloat() * 0.22f,
                drift = -1f + Random.nextFloat() * 2f,
                size = 6f + Random.nextFloat() * 8f,
                rotationSpeed = -240f + Random.nextFloat() * 480f,
                colorIndex = Random.nextInt(colors.size)
            )
        }
    }
    var clock by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(active) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            clock = ((now - start) / 1_000_000_000f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        pieces.forEach { p ->
            val t = (clock * p.fallSpeed) % 1.3f
            val y = size.height * (t - 0.15f)
            if (y < -20f || y > size.height + 20f) return@forEach
            val x = size.width * p.startX + p.drift * size.height * t * 0.15f
            rotate(degrees = clock * p.rotationSpeed, pivot = Offset(x, y)) {
                drawRect(
                    color = colors[p.colorIndex],
                    topLeft = Offset(x - p.size / 2, y - p.size / 2),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.5f)
                )
            }
        }
    }
}
