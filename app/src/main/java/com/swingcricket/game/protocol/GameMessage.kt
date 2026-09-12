package com.swingcricket.game.protocol

import com.swingcricket.game.game.BallResult
import com.swingcricket.game.game.DeliveryData
import com.swingcricket.game.game.Role
import kotlinx.serialization.Serializable

/**
 * Wire protocol exchanged over Nearby Connections between the two phones. Innings/match
 * transitions are intentionally NOT messages: both devices run an identical [com.swingcricket.game.game.MatchEngine]
 * and apply the exact same sequence of [Outcome]s, so match state (overs, target, winner) stays
 * in lockstep without extra synchronization.
 */
@Serializable
sealed class GameMessage {

    /** Sent by the toss winner's device once, right after the toss: tells the peer its role and the match length. */
    @Serializable
    data class RoleAssign(val recipientRole: Role, val oversPerInnings: Int) : GameMessage()

    /** Host flips a coin locally and reports the result so both screens can show the same animation. */
    @Serializable
    data class TossFlip(val hostWonToss: Boolean) : GameMessage()

    /** Bowler -> Batter: a ball has been released. */
    @Serializable
    data class BallBowled(val delivery: DeliveryData) : GameMessage()

    /** Batter -> Bowler: authoritative outcome of the ball just bowled (covers left-alone balls too). */
    @Serializable
    data class Outcome(val result: BallResult) : GameMessage()
}
