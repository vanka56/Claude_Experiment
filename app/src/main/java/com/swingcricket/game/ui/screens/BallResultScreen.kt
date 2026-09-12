package com.swingcricket.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swingcricket.game.game.BallResult
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.ui.components.GradientBackground
import com.swingcricket.game.ui.components.ResultBanner
import com.swingcricket.game.ui.components.ScoreBoard

@Composable
fun BallResultScreen(result: BallResult, innings: InningsRecord?, target: Int?, localIsBatting: Boolean) {
    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ResultBanner(result = result)
            Spacer(Modifier.height(40.dp))
            ScoreBoard(
                innings = innings,
                target = target,
                battingIsLocalLabel = if (localIsBatting) "You're batting" else "Opponent batting"
            )
        }
    }
}
