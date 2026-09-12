package com.swingcricket.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.SkyBlue
import com.swingcricket.game.ui.theme.StadiumNight
import com.swingcricket.game.ui.theme.StadiumNightElevated

/** Soft floodlight-glow backdrop used behind every screen for a consistent premium feel. */
@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(StadiumNightElevated, StadiumNight)))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(260.dp)
                .offset(x = 80.dp, y = (-80).dp)
                .blur(120.dp)
                .background(PitchGreen.copy(alpha = 0.16f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(320.dp)
                .offset(x = (-100).dp, y = 100.dp)
                .blur(140.dp)
                .background(SkyBlue.copy(alpha = 0.10f), CircleShape)
        )
        content()
    }
}
