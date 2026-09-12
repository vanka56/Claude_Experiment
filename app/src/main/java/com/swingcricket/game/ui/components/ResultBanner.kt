package com.swingcricket.game.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swingcricket.game.game.BallResult
import com.swingcricket.game.game.DismissalType
import com.swingcricket.game.ui.theme.FloodlightGold
import com.swingcricket.game.ui.theme.PitchGreen
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.TextSecondary
import com.swingcricket.game.ui.theme.WicketRed

@Composable
fun ResultBanner(result: BallResult, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = result,
        transitionSpec = {
            (scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) + fadeIn(tween(150)))
                .togetherWith(scaleOut(tween(150)) + fadeOut(tween(150)))
        },
        label = "resultBanner"
    ) { r ->
        val (h, c) = headlineFor(r)
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = h,
                style = MaterialTheme.typography.displayMedium,
                color = c,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = r.commentary,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun headlineFor(result: BallResult): Pair<String, Color> = when {
    result.isWicket -> (if (result.dismissal == DismissalType.BOWLED) "BOWLED!" else "OUT!") to WicketRed
    result.runs == 6 -> "SIX!" to FloodlightGold
    result.runs == 4 -> "FOUR!" to PitchGreen
    result.runs == 0 -> "DOT BALL" to TextPrimary
    else -> "${result.runs} RUN${if (result.runs > 1) "S" else ""}" to TextPrimary
}
