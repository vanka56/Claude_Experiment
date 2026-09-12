package com.swingcricket.game.game

/**
 * Pure, deterministic mapping from (delivery, shot) to a ball outcome. Deterministic on purpose:
 * the game is meant to feel skill-based — same swing quality against the same delivery always
 * produces the same result — rather than randomized, so players can learn and improve their timing.
 */
object OutcomeCalculator {

    fun compute(delivery: DeliveryData, shot: ShotData): BallResult {
        var timingFactor = (1f - kotlin.math.abs(shot.timingDeltaMs) / 300f).coerceIn(0f, 1f)
        if (delivery.swing != SwingType.STRAIGHT) timingFactor *= 0.88f

        val isWideLine = delivery.line == Line.WIDE_OFF || delivery.line == Line.WIDE_LEG
        val onStumps = delivery.line == Line.MIDDLE_STUMP || delivery.line == Line.LEG_STUMP ||
            delivery.line == Line.OFF_STUMP

        return when {
            timingFactor <= 0.15f -> {
                val bowled = onStumps || delivery.length == Length.YORKER
                if (bowled) {
                    dismissal(delivery, shot, DismissalType.BOWLED, "BOWLED! Right through the gate.")
                } else {
                    dismissal(delivery, shot, DismissalType.MISTIMED_CATCH, "Huge mistime... it's CAUGHT!")
                }
            }
            timingFactor <= 0.35f -> {
                if (delivery.length == Length.BOUNCER && shot.power > 0.6f) {
                    dismissal(delivery, shot, DismissalType.MISTIMED_CATCH, "Top-edged it... taken in the deep!")
                } else {
                    BallResult(
                        runs = 0, isWicket = false, dismissal = DismissalType.NONE, isBoundary = false,
                        commentary = "Solid defense. No run.", shot = shot, delivery = delivery
                    )
                }
            }
            else -> {
                val lengthFactor = when (delivery.length) {
                    Length.YORKER -> 0.70f
                    Length.FULL -> 1.05f
                    Length.GOOD -> 1.0f
                    Length.SHORT -> 1.10f
                    Length.BOUNCER -> 1.15f
                }
                val lineFactor = if (isWideLine) 0.85f else 1.0f
                val effective = (shot.power * timingFactor * lengthFactor * lineFactor).coerceIn(0f, 1f)
                val runs = bucketRuns(effective)
                val boundary = runs == 4 || runs == 6
                val commentary = when (runs) {
                    6 -> "SIX! That's out of the park!"
                    4 -> "Timed to perfection... FOUR!"
                    0 -> "Good ball, no run."
                    else -> "They run $runs."
                }
                BallResult(
                    runs = runs, isWicket = false, dismissal = DismissalType.NONE, isBoundary = boundary,
                    commentary = commentary, shot = shot, delivery = delivery
                )
            }
        }
    }

    /** The batter didn't swing before the ball's flight window elapsed. */
    fun leftBall(delivery: DeliveryData): BallResult = BallResult(
        runs = 0, isWicket = false, dismissal = DismissalType.NONE, isBoundary = false,
        commentary = "Left alone outside off.",
        shot = ShotData(timingDeltaMs = 999, power = 0f, direction = ShotDirection.NONE),
        delivery = delivery
    )

    private fun dismissal(delivery: DeliveryData, shot: ShotData, type: DismissalType, line: String) = BallResult(
        runs = 0, isWicket = true, dismissal = type, isBoundary = false,
        commentary = line, shot = shot, delivery = delivery
    )

    private fun bucketRuns(effective: Float): Int = when {
        effective < 0.15f -> 0
        effective < 0.35f -> 1
        effective < 0.55f -> 2
        effective < 0.70f -> 3
        effective < 0.85f -> 4
        else -> 6
    }
}
