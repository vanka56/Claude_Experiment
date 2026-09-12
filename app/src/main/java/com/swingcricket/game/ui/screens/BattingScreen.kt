package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.game.DeliveryData
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.sensors.SwingPhase
import com.swingcricket.game.ui.components.BallApproach
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.ScoreBoard
import com.swingcricket.game.ui.components.SwingMeter
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun BattingScreen(
    innings: InningsRecord?,
    target: Int?,
    inningsNumber: Int,
    pendingDelivery: DeliveryData?,
    ballFlightProgress: Float,
    swingPhase: SwingPhase,
    liveIntensity: Float,
) {
    GradientBackground {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            ScoreBoard(
                innings = innings,
                target = target,
                battingIsLocalLabel = if (inningsNumber == 1) "1st Innings · You're batting" else "2nd Innings · You're batting"
            )
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                BallApproach(delivery = pendingDelivery, progress = ballFlightProgress)
                Text(
                    text = if (pendingDelivery == null) "Get ready…" else "Swing NOW!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
                )
                if (pendingDelivery == null) {
                    Text(
                        text = "Wait for the bowler to release",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            SwingMeter(phase = swingPhase, intensity = liveIntensity, label = "Ready to bat")
        }
    }
}
