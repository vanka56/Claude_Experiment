package com.swingcricket.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.sensors.SwingPhase
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.ScoreBoard
import com.swingcricket.game.ui.components.SwingMeter
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.Motion
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun BowlingScreen(
    innings: InningsRecord?,
    target: Int?,
    inningsNumber: Int,
    swingPhase: SwingPhase,
    liveIntensity: Float,
    awaitingOutcome: Boolean,
) {
    GradientBackground {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            ScoreBoard(
                innings = innings,
                target = target,
                battingIsLocalLabel = if (inningsNumber == 1) "1st Innings · Opponent batting" else "2nd Innings · Opponent batting"
            )
            Spacer(Modifier.weight(1f))

            val ballScale by animateFloatAsState(
                targetValue = when (swingPhase) {
                    SwingPhase.LOADING -> 0.85f
                    SwingPhase.SWINGING -> 1.35f
                    SwingPhase.IMPACT -> 1.6f
                    else -> 1f
                },
                animationSpec = Motion.bouncy(),
                label = "ballScale"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚾",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.graphicsLayer { scaleX = ballScale; scaleY = ballScale }
                )
                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(visible = !awaitingOutcome) {
                    Text(
                        text = "Swing your phone like you're bowling",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                AnimatedVisibility(visible = awaitingOutcome) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PitchGreen, modifier = Modifier.padding(bottom = 16.dp))
                        Text(
                            text = "Ball away… waiting for the batter",
                            style = MaterialTheme.typography.titleMedium,
                            color = FloodlightGold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = "Tilt sideways for line, up/down for length, snap your wrist for swing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            SwingMeter(phase = swingPhase, intensity = liveIntensity, label = "Ready to bowl")
        }
    }
}
