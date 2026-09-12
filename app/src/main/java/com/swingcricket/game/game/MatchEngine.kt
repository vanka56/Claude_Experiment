package com.swingcricket.game.game

sealed class MatchEvent {
    data class BallApplied(val result: BallResult) : MatchEvent()
    data class OverComplete(val result: BallResult) : MatchEvent()
    data class InningsComplete(val justFinished: InningsRecord, val target: Int) : MatchEvent()
    data class MatchComplete(val summary: MatchSummary) : MatchEvent()
}

/**
 * Tracks match state for a single device. Both devices in a match run an identical MatchEngine
 * and stay in sync because every ball's [BallResult] is computed once (on the batting device) and
 * broadcast verbatim to the other — this class just applies results, it never invents them.
 */
class MatchEngine(
    private val config: MatchConfig,
    /** The role *this* device is playing in innings 1. */
    val localRoleInnings1: Role
) {
    var inningsNumber: Int = 1
        private set

    var first: InningsRecord = InningsRecord(batterIsLocal = localRoleInnings1 == Role.BATSMAN)
        private set

    var second: InningsRecord? = null
        private set

    var summary: MatchSummary? = null
        private set

    val currentInnings: InningsRecord get() = second ?: first
    val target: Int? get() = second?.let { first.runs + 1 }

    /** What the local device is doing in the innings currently being played. */
    val localRoleThisInnings: Role
        get() = if (currentInnings.batterIsLocal) Role.BATSMAN else Role.BOWLER

    fun applyBall(result: BallResult): MatchEvent {
        val updated = currentInnings.copy(
            runs = currentInnings.runs + result.runs,
            wickets = currentInnings.wickets + if (result.isWicket) 1 else 0,
            ballsBowled = currentInnings.ballsBowled + 1,
            history = currentInnings.history + result
        )
        if (inningsNumber == 1) first = updated else second = updated

        val maxBalls = config.oversPerInnings * 6
        val chased = inningsNumber == 2 && updated.runs >= (first.runs + 1)
        val inningsDone = updated.isAllOut || updated.ballsBowled >= maxBalls || chased

        return when {
            inningsDone && inningsNumber == 1 -> MatchEvent.InningsComplete(updated, first.runs + 1)
            inningsDone && inningsNumber == 2 -> {
                val s = buildSummary()
                summary = s
                MatchEvent.MatchComplete(s)
            }
            updated.ballsBowled % 6 == 0 -> MatchEvent.OverComplete(result)
            else -> MatchEvent.BallApplied(result)
        }
    }

    fun startSecondInnings() {
        inningsNumber = 2
        second = InningsRecord(batterIsLocal = !first.batterIsLocal)
    }

    private fun buildSummary(): MatchSummary {
        val f = first
        val s = second ?: InningsRecord(batterIsLocal = !f.batterIsLocal)
        val localInnings = if (f.batterIsLocal) f else s
        val oppInnings = if (f.batterIsLocal) s else f
        val isTie = f.runs == s.runs
        val localWon = localInnings.runs > oppInnings.runs

        val maxBalls = config.oversPerInnings * 6
        val margin = when {
            isTie -> "Match tied"
            s.runs > f.runs -> {
                val ballsToSpare = (maxBalls - s.ballsBowled).coerceAtLeast(0)
                "Won with $ballsToSpare ball${if (ballsToSpare == 1) "" else "s"} to spare"
            }
            else -> {
                val diff = f.runs - s.runs
                "Won by $diff run${if (diff == 1) "" else "s"}"
            }
        }

        return MatchSummary(
            winnerIsLocalPlayer = localWon,
            isTie = isTie,
            winnerName = if (isTie) "Nobody" else if (localWon) "You" else "Opponent",
            margin = margin,
            firstInnings = f,
            secondInnings = s
        )
    }
}
