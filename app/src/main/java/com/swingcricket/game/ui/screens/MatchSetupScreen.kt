package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.PrimaryButton
import com.swingcricket.game.ui.components.SecondaryButton
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary

@Composable
fun MatchSetupScreen(onConfirm: (oversPerInnings: Int, batFirst: Boolean) -> Unit) {
    var overs by remember { mutableFloatStateOf(2f) }
    var batFirst by remember { mutableStateOf(true) }

    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You won the toss!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Set up the match",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 40.dp)
            )

            Text("Overs per innings: ${overs.toInt()}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Slider(
                value = overs,
                onValueChange = { overs = it },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(thumbColor = PitchGreen, activeTrackColor = PitchGreen),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
            Text("Bat or bowl first?", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceChip(
                    label = "Bat first",
                    selected = batFirst,
                    onClick = { batFirst = true },
                    modifier = Modifier.weight(1f)
                )
                ChoiceChip(
                    label = "Bowl first",
                    selected = !batFirst,
                    onClick = { batFirst = false },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(40.dp))
            PrimaryButton(text = "Start Match", onClick = { onConfirm(overs.toInt(), batFirst) })
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        PrimaryButton(text = label, onClick = onClick, modifier = modifier)
    } else {
        SecondaryButton(text = label, onClick = onClick, modifier = modifier)
    }
}
