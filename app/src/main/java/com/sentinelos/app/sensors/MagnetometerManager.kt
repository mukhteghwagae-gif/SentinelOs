package com.sentinelos.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Safe magnetometer + accelerometer manager.
 * Handles missing hardware gracefully — no crash on devices without compass.
 */
class MagnetometerManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val linearAccelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isMagnetometerAvailable: Boolean get() = magneticSensor != null
    val isAccelerometerAvailable: Boolean get() = accelSensor != null
    val isGyroscopeAvailable: Boolean get() = gyroscopeSensor != null
    val isProximityAvailable: Boolean get() = proximitySensor != null
    val isLightAvailable: Boolean get() = lightSensor != null

    // Current sensor values
    var magneticField = FloatArray(3)      // x, y, z in µT
        private set
    var magneticStrength: Float = 0f       // Total field magnitude
        private set
    var acceleration = FloatArray(3)       // x, y, z in m/s²
        private set
    var linearAcceleration = FloatArray(3)
        private set
    var gyroscope = FloatArray(3)          // rad/s
        private set
    var proximity: Float = -1f
        private set
    var lightLevel: Float = -1f
        private set
    var azimuth: Float = 0f               // degrees 0-360
        private set

    // Anomaly detection
    var baselineMagnetic: Float = 0f      // Calibrated baseline
    var anomalyThreshold: Float = 50f     // µT deviation to trigger alert
    var isCalibrated: Boolean = false
        private set

    // Callbacks
    var onMagneticUpdate: ((x: Float, y: Float, z: Float, magnitude: Float) -> Unit)? = null
    var onAnomalyDetected: ((magnitude: Float, baseline: Float, delta: Float) -> Unit)? = null
    var onMotionDetected: ((ax: Float, ay: Float, az: Float, magnitude: Float) -> Unit)? = null
    var onProximityUpdate: ((distance: Float) -> Unit)? = null
    var onLightUpdate: ((lux: Float) -> Unit)? = null

    // History buffer for graphing (last 100 readings)
    val magneticHistory = ArrayDeque<Float>(100)
    val accelHistory = ArrayDeque<Float>(100)

    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true

        magneticSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        linearAccelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        sensorManager.unregisterListener(this)
    }

    /** Calibrate: take current magnetic reading as baseline */
    fun calibrate() {
        baselineMagnetic = magneticStrength
        isCalibrated = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticField[0] = event.values[0]
                magneticField[1] = event.values[1]
                magneticField[2] = event.values[2]
                magneticStrength = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                // Store history
                if (magneticHistory.size >= 100) magneticHistory.removeFirst()
                magneticHistory.addLast(magneticStrength)

                onMagneticUpdate?.invoke(
                    magneticField[0], magneticField[1], magneticField[2], magneticStrength
                )

                // Anomaly detection
                if (isCalibrated) {
                    val delta = Math.abs(magneticStrength - baselineMagnetic)
                    if (delta > anomalyThreshold) {
                        onAnomalyDetected?.invoke(magneticStrength, baselineMagnetic, delta)
                    }
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                acceleration[0] = event.values[0]
                acceleration[1] = event.values[1]
                acceleration[2] = event.values[2]
                val mag = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                if (accelHistory.size >= 100) accelHistory.removeFirst()
                accelHistory.addLast(mag)

                updateAzimuth()
            }

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linearAcceleration[0] = event.values[0]
                linearAcceleration[1] = event.values[1]
                linearAcceleration[2] = event.values[2]
                val mag = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                // Motion detection threshold > 2 m/s²
                if (mag > 2.0f) {
                    onMotionDetected?.invoke(
                        event.values[0], event.values[1], event.values[2], mag
                    )
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroscope[0] = event.values[0]
                gyroscope[1] = event.values[1]
                gyroscope[2] = event.values[2]
            }

            Sensor.TYPE_PROXIMITY -> {
                proximity = event.values[0]
                onProximityUpdate?.invoke(proximity)
            }

            Sensor.TYPE_LIGHT -> {
                lightLevel = event.values[0]
                onLightUpdate?.invoke(lightLevel)
            }
        }
    }

    private fun updateAzimuth() {
        if (magneticField.all { it == 0f } || acceleration.all { it == 0f }) return
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, acceleration, magneticField)) {
            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No action needed
    }

    fun getSensorSummary(): Map<String, String> = mapOf(
        "Magnetometer" to if (isMagnetometerAvailable) "%.1f µT".format(magneticStrength) else "N/A",
        "Accelerometer" to if (isAccelerometerAvailable) "%.2f, %.2f, %.2f m/s²".format(
            acceleration[0], acceleration[1], acceleration[2]) else "N/A",
        "Gyroscope" to if (isGyroscopeAvailable) "%.2f, %.2f, %.2f rad/s".format(
            gyroscope[0], gyroscope[1], gyroscope[2]) else "N/A",
        "Proximity" to if (isProximityAvailable && proximity >= 0) "%.1f cm".format(proximity) else "N/A",
        "Light" to if (isLightAvailable && lightLevel >= 0) "%.0f lux".format(lightLevel) else "N/A",
        "Azimuth" to "%.1f°".format(azimuth),
        "Calibrated" to if (isCalibrated) "Yes (baseline: %.1f µT)".format(baselineMagnetic) else "No"
    )
}
