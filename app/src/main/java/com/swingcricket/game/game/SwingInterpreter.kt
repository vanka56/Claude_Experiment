package com.swingcricket.game.game

import com.swingcricket.game.sensors.SwingEvent
import kotlin.math.roundToInt

/**
 * Turns raw physical [SwingEvent] measurements into cricket-domain data: a bowler's delivery,
 * or a batter's shot. Calibration constants here are the "feel" of the game — tune freely.
 */
object SwingInterpreter {

    fun interpretDelivery(event: SwingEvent): DeliveryData {
        val speedKmh = mapSpeed(event.peakAccelMagnitude)
        return DeliveryData(
            speedKmh = speedKmh,
            line = mapLine(event.yawDeltaDeg),
            length = mapLength(event.pitchDeltaDeg),
            swing = mapSwing(event.peakWristRateDegPerSec, event.yawDeltaDeg),
            releaseTimestampMs = System.currentTimeMillis(),
            flightTimeMs = flightTimeForSpeed(speedKmh)
        )
    }

    fun interpretShot(event: SwingEvent, idealImpactElapsedMs: Long): ShotData {
        val timingDelta = (event.impactElapsedRealtimeMs - idealImpactElapsedMs).toInt()
        val power = ((event.peakAccelMagnitude - 6f) / (36f - 6f)).coerceIn(0f, 1f)
        return ShotData(
            timingDeltaMs = timingDelta,
            power = power,
            direction = mapShotDirection(event.yawDeltaDeg)
        )
    }

    private fun mapSpeed(peakAccel: Float): Float {
        val t = ((peakAccel - 8f) / (40f - 8f)).coerceIn(0f, 1f)
        return (60f + t * 90f).let { (it * 10).roundToInt() / 10f }
    }

    private fun mapLine(yawDeltaDeg: Float): Line = when {
        yawDeltaDeg < -25f -> Line.WIDE_LEG
        yawDeltaDeg < -8f -> Line.LEG_STUMP
        yawDeltaDeg <= 8f -> Line.MIDDLE_STUMP
        yawDeltaDeg <= 25f -> Line.OFF_STUMP
        else -> Line.WIDE_OFF
    }

    private fun mapLength(pitchDeltaDeg: Float): Length = when {
        pitchDeltaDeg > 30f -> Length.YORKER
        pitchDeltaDeg > 15f -> Length.FULL
        pitchDeltaDeg > -5f -> Length.GOOD
        pitchDeltaDeg > -20f -> Length.SHORT
        else -> Length.BOUNCER
    }

    private fun mapSwing(wristRateDegPerSec: Float, yawDeltaDeg: Float): SwingType = when {
        wristRateDegPerSec < 200f -> SwingType.STRAIGHT
        yawDeltaDeg >= 0f -> SwingType.OUT_SWING
        else -> SwingType.IN_SWING
    }

    private fun mapShotDirection(yawDeltaDeg: Float): ShotDirection = when {
        yawDeltaDeg < -40f -> ShotDirection.FINE_LEG
        yawDeltaDeg < -15f -> ShotDirection.LEG
        yawDeltaDeg < -5f -> ShotDirection.MID_WICKET
        yawDeltaDeg <= 5f -> ShotDirection.STRAIGHT
        yawDeltaDeg <= 15f -> ShotDirection.COVER
        yawDeltaDeg <= 40f -> ShotDirection.OFF
        else -> ShotDirection.THIRD_MAN
    }

    /** Pitch length is scaled down from a real 18–20m for a punchier, more playable reaction window. */
    private fun flightTimeForSpeed(speedKmh: Float): Long {
        val speedMs = speedKmh / 3.6f
        val ms = (16_000f / speedMs).roundToInt()
        return ms.toLong().coerceIn(550L, 1400L)
    }
}
