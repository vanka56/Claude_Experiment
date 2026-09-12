package com.swingcricket.game.game

import kotlinx.serialization.Serializable

/** Which role a player is performing for the current innings. */
enum class Role { BOWLER, BATSMAN }

/** Horizontal line of a delivery, derived from the bowler's release yaw. */
enum class Line { LEG_STUMP, MIDDLE_STUMP, OFF_STUMP, WIDE_OFF, WIDE_LEG }

/** Length of a delivery, derived from the bowler's release pitch angle. */
enum class Length { YORKER, FULL, GOOD, SHORT, BOUNCER }

/** Swing/seam movement imparted by the bowler's wrist rotation at release. */
enum class SwingType { STRAIGHT, IN_SWING, OUT_SWING }

/** Direction the batter's shot travels, derived from bat swing angle at impact. */
enum class ShotDirection {
    NONE, LEG, MID_WICKET, STRAIGHT, COVER, OFF, THIRD_MAN, FINE_LEG
}

/** How a batter got out. */
enum class DismissalType { NONE, BOWLED, MISTIMED_CATCH, EARLY_SWING, LATE_SWING }

@Serializable
data class DeliveryData(
    val speedKmh: Float,
    val line: Line,
    val length: Length,
    val swing: SwingType,
    /** Epoch-millis timestamp (bowler device clock) marking the instant of release. */
    val releaseTimestampMs: Long,
    /** Estimated flight time from release to the batting crease, in ms. */
    val flightTimeMs: Long
)

@Serializable
data class ShotData(
    /** Difference between the batter's swing-impact instant and the ideal impact instant, ms. Negative = early. */
    val timingDeltaMs: Int,
    /** 0f..1f power of the swing. */
    val power: Float,
    val direction: ShotDirection
)

@Serializable
data class BallResult(
    val runs: Int,
    val isWicket: Boolean,
    val dismissal: DismissalType,
    val isBoundary: Boolean,
    val commentary: String,
    val shot: ShotData,
    val delivery: DeliveryData
)

data class InningsRecord(
    /** True if the local player is the one batting this innings (the other device is bowling). */
    val batterIsLocal: Boolean,
    val runs: Int = 0,
    val wickets: Int = 0,
    val ballsBowled: Int = 0,
    val history: List<BallResult> = emptyList()
) {
    val oversText: String
        get() = "${ballsBowled / 6}.${ballsBowled % 6}"

    /** In this 1-vs-1 arcade format a single wicket ends the innings. */
    val isAllOut: Boolean get() = wickets > 0
}

data class MatchConfig(val oversPerInnings: Int)

data class MatchSummary(
    val winnerIsLocalPlayer: Boolean,
    val isTie: Boolean,
    val winnerName: String,
    val margin: String,
    val firstInnings: InningsRecord,
    val secondInnings: InningsRecord
)
