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
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.PrimaryButton
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun InningsBreakScreen(justFinished: InningsRecord, target: Int, nextIsLocalBatting: Boolean, onContinue: () -> Unit) {
    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Innings Break",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${justFinished.runs}/${justFinished.wickets} from ${justFinished.oversText} overs",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Target: $target",
                style = MaterialTheme.typography.displayMedium,
                color = FloodlightGold,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (nextIsLocalBatting) "You're up — chase it down!" else "You're bowling — defend it!",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
            PrimaryButton(text = "Continue", onClick = onContinue)
        }
    }
}
