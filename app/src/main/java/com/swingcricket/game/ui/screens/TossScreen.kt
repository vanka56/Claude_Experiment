package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.components.CoinFlip
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun TossScreen(flipping: Boolean, hostWon: Boolean?, localWonToss: Boolean, opponentName: String) {
    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The Toss",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(40.dp))
            CoinFlip(flipping = flipping, hostWon = hostWon)
            Spacer(Modifier.height(40.dp))
            Text(
                text = when {
                    flipping -> "Flipping the coin…"
                    localWonToss -> "You won the toss!"
                    else -> "$opponentName won the toss"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = if (!flipping && localWonToss) FloodlightGold else TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (!flipping && !localWonToss) {
                Text(
                    text = "Waiting for $opponentName to choose bat or bowl…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
