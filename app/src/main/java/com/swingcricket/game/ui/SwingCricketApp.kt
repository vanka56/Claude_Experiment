package com.swingcricket.game.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swingcricket.game.game.Role
import com.swingcricket.game.ui.screens.BallResultScreen
import com.swingcricket.game.ui.screens.BattingScreen
import com.swingcricket.game.ui.screens.BowlingScreen
import com.swingcricket.game.ui.screens.ConnectingScreen
import com.swingcricket.game.ui.screens.HomeScreen
import com.swingcricket.game.ui.screens.InningsBreakScreen
import com.swingcricket.game.ui.screens.MatchOverScreen
import com.swingcricket.game.ui.screens.MatchSetupScreen
import com.swingcricket.game.ui.screens.TossScreen
import com.swingcricket.game.ui.theme.SwingCricketTheme
import com.swingcricket.game.ui.theme.TextPrimary
import com.swingcricket.game.ui.theme.WicketRed
import com.swingcricket.game.viewmodel.GameViewModel
import com.swingcricket.game.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SwingCricketApp(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SwingCricketTheme {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = {
                    (slideInHorizontally(tween(320)) { it / 6 } + fadeIn(tween(320)))
                        .togetherWith(fadeOut(tween(180)))
                },
                label = "screen"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        playerName = state.playerName,
                        onNameChange = viewModel::setPlayerName,
                        onHost = viewModel::hostGame,
                        onJoin = viewModel::joinGame,
                        onPermissionDenied = { /* surfaced via errorMessage on next attempt */ }
                    )
                    Screen.CONNECTING -> ConnectingScreen(
                        isHost = state.isHost,
                        connectionState = state.connectionState,
                        onCancel = viewModel::leaveGame
                    )
                    Screen.TOSS -> TossScreen(
                        flipping = state.coinFlipping,
                        hostWon = state.tossHostWon,
                        localWonToss = state.localWonToss,
                        opponentName = state.opponentName
                    )
                    Screen.MATCH_SETUP -> MatchSetupScreen(onConfirm = viewModel::confirmMatchSetup)
                    Screen.BOWLING -> BowlingScreen(
                        innings = state.innings,
                        target = state.target,
                        inningsNumber = state.inningsNumber,
                        swingPhase = state.swingPhase,
                        liveIntensity = state.liveIntensity,
                        awaitingOutcome = state.awaitingOutcome
                    )
                    Screen.BATTING -> BattingScreen(
                        innings = state.innings,
                        target = state.target,
                        inningsNumber = state.inningsNumber,
                        pendingDelivery = state.pendingDelivery,
                        ballFlightProgress = state.ballFlightProgress,
                        swingPhase = state.swingPhase,
                        liveIntensity = state.liveIntensity
                    )
                    Screen.BALL_RESULT -> state.lastBall?.let {
                        BallResultScreen(
                            result = it,
                            innings = state.innings,
                            target = state.target,
                            localIsBatting = state.localRole == Role.BATSMAN
                        )
                    }
                    Screen.INNINGS_BREAK -> state.target?.let { t ->
                        state.innings?.let { justFinished ->
                            InningsBreakScreen(
                                justFinished = justFinished,
                                target = t,
                                nextIsLocalBatting = state.localRole == Role.BOWLER,
                                onContinue = viewModel::proceedFromInningsBreak
                            )
                        }
                    }
                    Screen.MATCH_OVER -> state.matchSummary?.let {
                        MatchOverScreen(
                            summary = it,
                            isHost = state.isHost,
                            onRematch = viewModel::playAgainAsHost,
                            onHome = viewModel::leaveGame
                        )
                    }
                }
            }

            state.errorMessage?.let { message ->
                ErrorBanner(message = message, onDismiss = viewModel::dismissError, modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(message) {
        delay(3200)
        onDismiss()
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WicketRed.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
