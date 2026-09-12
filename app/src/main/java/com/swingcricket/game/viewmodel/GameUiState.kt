package com.swingcricket.game.viewmodel

import com.swingcricket.game.connection.ConnectionState
import com.swingcricket.game.game.BallResult
import com.swingcricket.game.game.DeliveryData
import com.swingcricket.game.game.InningsRecord
import com.swingcricket.game.game.MatchSummary
import com.swingcricket.game.game.Role
import com.swingcricket.game.sensors.SwingPhase

enum class Screen {
    HOME, CONNECTING, TOSS, MATCH_SETUP, BOWLING, BATTING, BALL_RESULT, INNINGS_BREAK, MATCH_OVER
}

data class GameUiState(
    val screen: Screen = Screen.HOME,
    val playerName: String = "",
    val isHost: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.Idle,
    val opponentName: String = "",

    // Toss
    val coinFlipping: Boolean = false,
    val tossHostWon: Boolean? = null,
    val localWonToss: Boolean = false,

    // Match setup
    val oversPerInnings: Int = 2,

    // Live gameplay
    val localRole: Role = Role.BATSMAN,
    val inningsNumber: Int = 1,
    val innings: InningsRecord? = null,
    val target: Int? = null,
    val awaitingOutcome: Boolean = false,
    val pendingDelivery: DeliveryData? = null,
    val ballFlightProgress: Float = 0f,
    val swingPhase: SwingPhase = SwingPhase.IDLE,
    val liveIntensity: Float = 0f,
    val lastBall: BallResult? = null,
    val lastBallWasOverEnd: Boolean = false,

    val matchSummary: MatchSummary? = null,
    val errorMessage: String? = null
)
