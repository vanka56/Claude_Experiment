package com.swingcricket.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.StadiumCard
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary
import com.swingcricket.game.ui.theme.WicketRed

@Composable
fun ScoreBoard(
    innings: InningsRecord?,
    target: Int?,
    battingIsLocalLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StadiumCard)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(battingIsLocalLabel, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            if (target != null) {
                Text(
                    "Target $target",
                    style = MaterialTheme.typography.labelLarge,
                    color = FloodlightGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${innings?.runs ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "/${innings?.wickets ?: 0}",
                style = MaterialTheme.typography.headlineMedium,
                color = if ((innings?.wickets ?: 0) > 0) WicketRed else TextSecondary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "(${innings?.oversText ?: "0.0"} ov)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}
