package com.swingcricket.game.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SwingPhase { IDLE, LOADING, SWINGING, IMPACT, RECOVERING }

/** Raw physical measurements captured for one complete swing gesture. Game-semantic mapping happens elsewhere. */
data class SwingEvent(
    val peakAccelMagnitude: Float,
    val yawDeltaDeg: Float,
    val pitchDeltaDeg: Float,
    val peakWristRateDegPerSec: Float,
    val impactElapsedRealtimeMs: Long,
    val gestureDurationMs: Long
)

/**
 * Detects a single "swing" gesture (bat swing or bowling arm action) from the phone's
 * linear-acceleration, gyroscope and rotation-vector sensors, and reports the physical
 * characteristics of that swing at the moment of peak effort ("impact").
 */
class SwingDetector(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _phase = MutableStateFlow(SwingPhase.IDLE)
    val phase: StateFlow<SwingPhase> = _phase.asStateFlow()

    /** Normalized 0f..1f instantaneous swing intensity, for driving a live power meter in the UI. */
    private val _liveIntensity = MutableStateFlow(0f)
    val liveIntensity: StateFlow<Float> = _liveIntensity.asStateFlow()

    var onSwingComplete: ((SwingEvent) -> Unit)? = null

    private var listening = false

    private var gestureStartRealtimeMs = 0L
    private var gestureStartYaw = 0f
    private var gestureStartPitch = 0f
    private var currentYaw = 0f
    private var currentPitch = 0f

    private var peakAccelMag = 0f
    private var peakWristRate = 0f
    private var yawAtPeak = 0f
    private var pitchAtPeak = 0f

    private val magHistory = FloatArray(3)
    private var recoveryStableSince = 0L

    private val rotationMatrix = FloatArray(9)
    private val orientationRad = FloatArray(3)

    fun start() {
        if (listening) return
        listening = true
        reset()
        linearAccelSensor?.let {
            sensorManager.registerListener(accelListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        rotationVectorSensor?.let {
            sensorManager.registerListener(rotationListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!listening) return
        listening = false
        sensorManager.unregisterListener(accelListener)
        sensorManager.unregisterListener(gyroListener)
        sensorManager.unregisterListener(rotationListener)
        reset()
    }

    private fun reset() {
        _phase.value = SwingPhase.IDLE
        _liveIntensity.value = 0f
        peakAccelMag = 0f
        peakWristRate = 0f
        magHistory.fill(0f)
        recoveryStableSince = 0L
    }

    private val rotationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationRad)
            currentYaw = Math.toDegrees(orientationRad[0].toDouble()).toFloat()
            currentPitch = Math.toDegrees(orientationRad[1].toDouble()).toFloat()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private val gyroListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rateDegPerSec = Math.toDegrees(
                sqrt(
                    (event.values[0] * event.values[0] +
                        event.values[1] * event.values[1]).toDouble()
                )
            ).toFloat()
            if (_phase.value == SwingPhase.LOADING || _phase.value == SwingPhase.SWINGING) {
                if (rateDegPerSec > peakWristRate) peakWristRate = rateDegPerSec
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private val accelListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val mag = sqrt(
                event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
            )
            val now = SystemClock.elapsedRealtime()
            magHistory[0] = magHistory[1]
            magHistory[1] = magHistory[2]
            magHistory[2] = mag

            when (_phase.value) {
                SwingPhase.IDLE -> {
                    if (mag > BACKSWING_THRESHOLD) {
                        _phase.value = SwingPhase.LOADING
                        gestureStartRealtimeMs = now
                        gestureStartYaw = currentYaw
                        gestureStartPitch = currentPitch
                        peakAccelMag = mag
                        peakWristRate = 0f
                        yawAtPeak = currentYaw
                        pitchAtPeak = currentPitch
                    }
                }
                SwingPhase.LOADING, SwingPhase.SWINGING -> {
                    if (mag > IMPACT_MIN_THRESHOLD) _phase.value = SwingPhase.SWINGING
                    if (mag > peakAccelMag) {
                        peakAccelMag = mag
                        yawAtPeak = currentYaw
                        pitchAtPeak = currentPitch
                    }
                    val elapsed = now - gestureStartRealtimeMs
                    val isPeak = magHistory[1] > magHistory[0] && magHistory[1] >= magHistory[2]
                    if (_phase.value == SwingPhase.SWINGING &&
                        elapsed > MIN_GESTURE_MS &&
                        isPeak &&
                        magHistory[1] > IMPACT_MIN_THRESHOLD
                    ) {
                        emitImpact(now, elapsed)
                    } else if (elapsed > MAX_GESTURE_MS) {
                        // Gesture ran long without a clean peak; use whatever we have.
                        emitImpact(now, elapsed)
                    }
                }
                SwingPhase.IMPACT -> {
                    // brief single-frame state, next sample moves to RECOVERING
                    _phase.value = SwingPhase.RECOVERING
                    recoveryStableSince = 0L
                }
                SwingPhase.RECOVERING -> {
                    if (mag < RESET_THRESHOLD) {
                        if (recoveryStableSince == 0L) recoveryStableSince = now
                        if (now - recoveryStableSince > COOLDOWN_MS) {
                            reset()
                        }
                    } else {
                        recoveryStableSince = 0L
                    }
                }
            }

            _liveIntensity.value = (mag / INTENSITY_NORMALIZER).coerceIn(0f, 1f)
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private fun emitImpact(nowMs: Long, elapsedMs: Long) {
        _phase.value = SwingPhase.IMPACT
        val yawDelta = angleDelta(gestureStartYaw, yawAtPeak)
        val pitchDelta = angleDelta(gestureStartPitch, pitchAtPeak)
        onSwingComplete?.invoke(
            SwingEvent(
                peakAccelMagnitude = peakAccelMag,
                yawDeltaDeg = yawDelta,
                pitchDeltaDeg = pitchDelta,
                peakWristRateDegPerSec = peakWristRate,
                impactElapsedRealtimeMs = nowMs,
                gestureDurationMs = elapsedMs
            )
        )
    }

    private fun angleDelta(from: Float, to: Float): Float {
        var delta = to - from
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        return delta
    }

    companion object {
        private const val BACKSWING_THRESHOLD = 3.0f
        private const val IMPACT_MIN_THRESHOLD = 7.5f
        private const val RESET_THRESHOLD = 1.5f
        private const val MIN_GESTURE_MS = 70L
        private const val MAX_GESTURE_MS = 900L
        private const val COOLDOWN_MS = 180L
        private const val INTENSITY_NORMALIZER = 30f
    }
}
