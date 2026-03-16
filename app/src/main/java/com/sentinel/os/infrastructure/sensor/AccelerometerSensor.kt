package com.sentinel.os.infrastructure.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import kotlin.math.sqrt

data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Accelerometer sensor for vibration and motion detection.
 * Reads at high sampling rate for impulse detection.
 */
class AccelerometerSensor(context: Context) : BaseSensor<AccelerometerData>(), SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val _accelerometerFlow = MutableSharedFlow<AccelerometerData>()
    override fun getSensorData(): Flow<AccelerometerData> = _accelerometerFlow.asSharedFlow()

    private val gravityFilter = FloatArray(3) { 9.81f }
    private val alpha = 0.8f

    override fun startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            _isActive.value = true
            Timber.d("Accelerometer started")
        } else {
            setError("Accelerometer not available")
        }
    }

    override fun stopListening() {
        sensorManager.unregisterListener(this)
        _isActive.value = false
        Timber.d("Accelerometer stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Apply high-pass filter to remove gravity component
            gravityFilter[0] = alpha * gravityFilter[0] + (1 - alpha) * event.values[0]
            gravityFilter[1] = alpha * gravityFilter[1] + (1 - alpha) * event.values[1]
            gravityFilter[2] = alpha * gravityFilter[2] + (1 - alpha) * event.values[2]

            val x = event.values[0] - gravityFilter[0]
            val y = event.values[1] - gravityFilter[1]
            val z = event.values[2] - gravityFilter[2]

            val magnitude = sqrt(x * x + y * y + z * z)
            val data = AccelerometerData(x, y, z, magnitude)

            updateData(data)
            try {
                _accelerometerFlow.tryEmit(data)
            } catch (e: Exception) {
                Timber.e(e, "Error emitting accelerometer data")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Timber.d("Accelerometer accuracy changed: $accuracy")
    }

    fun detectImpulse(threshold: Float = 2.0f): Boolean {
        val current = _sensorData.value ?: return false
        return current.magnitude > threshold
    }
}
