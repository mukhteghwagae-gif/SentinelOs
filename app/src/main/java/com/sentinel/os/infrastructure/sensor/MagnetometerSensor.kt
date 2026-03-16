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

data class MagnetometerData(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Magnetometer sensor implementation for reading magnetic field data.
 * Emits data at the highest available sampling rate.
 */
class MagnetometerSensor(context: Context) : BaseSensor<MagnetometerData>(), SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val _magnetometerFlow = MutableSharedFlow<MagnetometerData>()
    override fun getSensorData(): Flow<MagnetometerData> = _magnetometerFlow.asSharedFlow()

    private var baselineX = 0f
    private var baselineY = 0f
    private var baselineZ = 0f
    private var isCalibrated = false

    override fun startListening() {
        if (magnetometer != null) {
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            _isActive.value = true
            Timber.d("Magnetometer started")
        } else {
            setError("Magnetometer not available")
        }
    }

    override fun stopListening() {
        sensorManager.unregisterListener(this)
        _isActive.value = false
        Timber.d("Magnetometer stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calibrate on first reading
            if (!isCalibrated) {
                baselineX = x
                baselineY = y
                baselineZ = z
                isCalibrated = true
            }

            val magnitude = sqrt(x * x + y * y + z * z)
            val data = MagnetometerData(x, y, z, magnitude)

            updateData(data)
            try {
                _magnetometerFlow.tryEmit(data)
            } catch (e: Exception) {
                Timber.e(e, "Error emitting magnetometer data")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Timber.d("Magnetometer accuracy changed: $accuracy")
    }

    fun getDeviation(): Triple<Float, Float, Float> {
        val current = _sensorData.value ?: return Triple(0f, 0f, 0f)
        return Triple(
            current.x - baselineX,
            current.y - baselineY,
            current.z - baselineZ
        )
    }
}
