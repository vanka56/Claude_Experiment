package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.swingcricket.game.game.MatchSummary
import com.swingcricket.game.ui.components.Confetti
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.PrimaryButton
import com.swingcricket.game.ui.components.SecondaryButton
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary
import com.swingcricket.game.ui.theme.WicketRed

@Composable
fun MatchOverScreen(
    summary: MatchSummary,
    isHost: Boolean,
    onRematch: () -> Unit,
    onHome: () -> Unit,
) {
    GradientBackground {
        Confetti(active = summary.winnerIsLocalPlayer && !summary.isTie)
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    summary.isTie -> "IT'S A TIE!"
                    summary.winnerIsLocalPlayer -> "YOU WIN!"
                    else -> "YOU LOSE"
                },
                style = MaterialTheme.typography.displayMedium,
                color = if (summary.winnerIsLocalPlayer && !summary.isTie) FloodlightGold else if (summary.isTie) TextPrimary else WicketRed,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = summary.margin,
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InningsSummaryColumn(
                    label = if (summary.firstInnings.batterIsLocal) "You" else "Opponent",
                    runs = summary.firstInnings.runs,
                    wickets = summary.firstInnings.wickets,
                    overs = summary.firstInnings.oversText
                )
                InningsSummaryColumn(
                    label = if (summary.secondInnings.batterIsLocal) "You" else "Opponent",
                    runs = summary.secondInnings.runs,
                    wickets = summary.secondInnings.wickets,
                    overs = summary.secondInnings.oversText
                )
            }

            Spacer(Modifier.height(48.dp))
            if (isHost) {
                PrimaryButton(text = "Rematch", onClick = onRematch)
            } else {
                Text(
                    text = "Waiting for host to start a rematch…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(14.dp))
            SecondaryButton(text = "Back to Home", onClick = onHome)
        }
    }
}

@Composable
private fun InningsSummaryColumn(label: String, runs: Int, wickets: Int, overs: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Text(
            "$runs/$wickets",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text("$overs ov", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
