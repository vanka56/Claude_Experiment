package com.swingcricket.game.viewmodel

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swingcricket.game.connection.ConnectionState
import com.swingcricket.game.connection.NearbyConnectionManager
import com.swingcricket.game.game.BallResult
import com.swingcricket.game.game.DeliveryData
import com.swingcricket.game.game.MatchConfig
import com.swingcricket.game.game.MatchEngine
import com.swingcricket.game.game.MatchEvent
import com.swingcricket.game.game.OutcomeCalculator
import com.swingcricket.game.game.Role
import com.swingcricket.game.game.SwingInterpreter
import com.swingcricket.game.protocol.GameMessage
import com.swingcricket.game.sensors.SwingDetector
import com.swingcricket.game.sensors.SwingEvent
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val COIN_FLIP_DURATION_MS = 1800L
private const val RESULT_DISPLAY_MS = 2200L
private const val TOSS_START_DELAY_MS = 350L

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(
        GameUiState(playerName = "Player${Random.nextInt(1000, 9999)}")
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val connection = NearbyConnectionManager(app, _uiState.value.playerName)
    private val swingDetector = SwingDetector(app)
    private val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private var matchEngine: MatchEngine? = null
    private var ballFlightJob: Job? = null
    private var pendingIdealImpactElapsedMs: Long = 0L

    init {
        viewModelScope.launch {
            connection.state.collect { handleConnectionState(it) }
        }
        viewModelScope.launch {
            connection.messages.collect { handleMessage(it) }
        }
        viewModelScope.launch {
            swingDetector.phase.collect { p -> _uiState.update { it.copy(swingPhase = p) } }
        }
        viewModelScope.launch {
            swingDetector.liveIntensity.collect { v -> _uiState.update { it.copy(liveIntensity = v) } }
        }
        swingDetector.onSwingComplete = { onSwingComplete(it) }
    }

    // ---- Player / connection setup ----

    fun setPlayerName(name: String) {
        val trimmed = name.trim()
        _uiState.update { it.copy(playerName = trimmed) }
        connection.localName = trimmed.ifBlank { _uiState.value.playerName }
    }

    fun hostGame() {
        _uiState.update { it.copy(isHost = true, screen = Screen.CONNECTING, errorMessage = null) }
        connection.host()
    }

    fun joinGame() {
        _uiState.update { it.copy(isHost = false, screen = Screen.CONNECTING, errorMessage = null) }
        connection.join()
    }

    fun leaveGame() {
        cleanupForNewSession()
        connection.disconnect()
        _uiState.value = GameUiState(playerName = _uiState.value.playerName)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun handleConnectionState(state: ConnectionState) {
        _uiState.update { it.copy(connectionState = state) }
        when (state) {
            is ConnectionState.Connected -> {
                _uiState.update {
                    it.copy(opponentName = state.endpointName, screen = Screen.TOSS, coinFlipping = true)
                }
                if (_uiState.value.isHost) beginToss()
            }
            is ConnectionState.Failed -> _uiState.update {
                it.copy(errorMessage = state.reason, screen = Screen.HOME)
            }
            is ConnectionState.Disconnected -> {
                if (_uiState.value.screen != Screen.HOME) {
                    cleanupForNewSession()
                    _uiState.update {
                        GameUiState(playerName = it.playerName, errorMessage = "Opponent disconnected")
                    }
                }
            }
            else -> Unit
        }
    }

    private fun cleanupForNewSession() {
        ballFlightJob?.cancel()
        swingDetector.stop()
        matchEngine = null
    }

    // ---- Toss ----

    private fun beginToss() {
        viewModelScope.launch {
            delay(TOSS_START_DELAY_MS)
            val hostWon = Random.nextBoolean()
            connection.send(GameMessage.TossFlip(hostWon))
            applyTossResult(hostWon)
        }
    }

    private fun applyTossResult(hostWon: Boolean) {
        val localWon = hostWon == _uiState.value.isHost
        _uiState.update { it.copy(screen = Screen.TOSS, coinFlipping = true, tossHostWon = hostWon) }
        viewModelScope.launch {
            delay(COIN_FLIP_DURATION_MS)
            _uiState.update {
                it.copy(
                    coinFlipping = false,
                    localWonToss = localWon,
                    screen = if (localWon) Screen.MATCH_SETUP else Screen.TOSS
                )
            }
        }
    }

    fun confirmMatchSetup(oversPerInnings: Int, batFirst: Boolean) {
        val localRole = if (batFirst) Role.BATSMAN else Role.BOWLER
        val peerRole = if (batFirst) Role.BOWLER else Role.BATSMAN
        connection.send(GameMessage.RoleAssign(peerRole, oversPerInnings))
        startMatch(localRole, oversPerInnings)
    }

    private fun startMatch(localRoleInnings1: Role, oversPerInnings: Int) {
        matchEngine = MatchEngine(MatchConfig(oversPerInnings), localRoleInnings1)
        _uiState.update { it.copy(oversPerInnings = oversPerInnings, inningsNumber = 1) }
        goToRoleScreen()
    }

    private fun goToRoleScreen() {
        val engine = matchEngine ?: return
        val role = engine.localRoleThisInnings
        _uiState.update {
            it.copy(
                screen = if (role == Role.BOWLER) Screen.BOWLING else Screen.BATTING,
                localRole = role,
                awaitingOutcome = false,
                pendingDelivery = null,
                ballFlightProgress = 0f,
                lastBall = null,
                innings = engine.currentInnings,
                target = engine.target,
                inningsNumber = engine.inningsNumber
            )
        }
        swingDetector.start()
    }

    fun proceedFromInningsBreak() {
        val engine = matchEngine ?: return
        engine.startSecondInnings()
        goToRoleScreen()
    }

    fun playAgainAsHost() {
        if (!_uiState.value.isHost) return
        cleanupForNewSession()
        beginToss()
    }

    // ---- Networked messages ----

    private fun handleMessage(message: GameMessage) {
        when (message) {
            is GameMessage.TossFlip -> applyTossResult(message.hostWonToss)
            is GameMessage.RoleAssign -> startMatch(message.recipientRole, message.oversPerInnings)
            is GameMessage.BallBowled -> onBallBowled(message.delivery)
            is GameMessage.Outcome -> onOutcomeReceived(message.result)
        }
    }

    // ---- Bowling (local device is the bowler this innings) ----

    private fun handleBowlerSwing(event: SwingEvent) {
        val delivery = SwingInterpreter.interpretDelivery(event)
        connection.send(GameMessage.BallBowled(delivery))
        vibrate(30)
        swingDetector.stop()
        _uiState.update { it.copy(awaitingOutcome = true, pendingDelivery = delivery) }
    }

    private fun onOutcomeReceived(result: BallResult) {
        val engine = matchEngine ?: return
        val event = engine.applyBall(result)
        presentBallResult(result, event)
    }

    // ---- Batting (local device is the batter this innings) ----

    private fun onBallBowled(delivery: DeliveryData) {
        val receivedAtElapsed = SystemClock.elapsedRealtime()
        pendingIdealImpactElapsedMs = receivedAtElapsed + delivery.flightTimeMs
        _uiState.update { it.copy(pendingDelivery = delivery, ballFlightProgress = 0f) }

        ballFlightJob?.cancel()
        ballFlightJob = viewModelScope.launch {
            val start = SystemClock.elapsedRealtime()
            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - start
                val progress = (elapsed.toFloat() / delivery.flightTimeMs).coerceIn(0f, 1f)
                _uiState.update { it.copy(ballFlightProgress = progress) }
                if (progress >= 1f) break
                delay(12)
            }
            if (_uiState.value.pendingDelivery != null) {
                resolveBatterOutcome(OutcomeCalculator.leftBall(delivery))
            }
        }
    }

    private fun handleBatterSwing(event: SwingEvent) {
        val delivery = _uiState.value.pendingDelivery ?: return
        ballFlightJob?.cancel()
        val shot = SwingInterpreter.interpretShot(event, pendingIdealImpactElapsedMs)
        val result = OutcomeCalculator.compute(delivery, shot)
        resolveBatterOutcome(result)
    }

    private fun resolveBatterOutcome(result: BallResult) {
        swingDetector.stop()
        connection.send(GameMessage.Outcome(result))
        val engine = matchEngine ?: return
        val event = engine.applyBall(result)
        _uiState.update { it.copy(pendingDelivery = null) }
        presentBallResult(result, event)
    }

    // ---- Shared result presentation ----

    private fun onSwingComplete(event: SwingEvent) {
        val state = _uiState.value
        when {
            state.screen == Screen.BOWLING && !state.awaitingOutcome -> handleBowlerSwing(event)
            state.screen == Screen.BATTING && state.pendingDelivery != null -> handleBatterSwing(event)
            else -> Unit
        }
    }

    private fun presentBallResult(result: BallResult, event: MatchEvent) {
        val engine = matchEngine ?: return
        vibrateForResult(result)
        swingDetector.stop()
        _uiState.update {
            it.copy(
                screen = Screen.BALL_RESULT,
                lastBall = result,
                lastBallWasOverEnd = event is MatchEvent.OverComplete,
                innings = engine.currentInnings,
                target = engine.target,
                awaitingOutcome = false
            )
        }
        viewModelScope.launch {
            delay(RESULT_DISPLAY_MS)
            when (event) {
                is MatchEvent.InningsComplete -> _uiState.update {
                    it.copy(screen = Screen.INNINGS_BREAK, target = event.target)
                }
                is MatchEvent.MatchComplete -> _uiState.update {
                    it.copy(screen = Screen.MATCH_OVER, matchSummary = event.summary)
                }
                else -> goToRoleScreen()
            }
        }
    }

    // ---- Haptics ----

    private fun vibrate(ms: Long) {
        vibrator?.let { v -> if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    private fun vibrateForResult(result: BallResult) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val effect = when {
            result.isWicket -> VibrationEffect.createOneShot(260, VibrationEffect.DEFAULT_AMPLITUDE)
            result.runs == 6 -> VibrationEffect.createWaveform(longArrayOf(0, 90, 60, 90, 60, 140), -1)
            result.isBoundary -> VibrationEffect.createWaveform(longArrayOf(0, 90, 70, 110), -1)
            result.runs > 0 -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
            else -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE / 2)
        }
        v.vibrate(effect)
    }

    override fun onCleared() {
        super.onCleared()
        swingDetector.stop()
        connection.disconnect()
    }
}
