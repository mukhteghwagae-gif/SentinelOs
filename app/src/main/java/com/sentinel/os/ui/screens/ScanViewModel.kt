package com.sentinel.os.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.os.infrastructure.sensor.MagnetometerSensor
import com.sentinel.os.infrastructure.sensor.AccelerometerSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val magnetometerSensor = MagnetometerSensor(application)
    private val accelerometerSensor = AccelerometerSensor(application)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _magneticFieldStrength = MutableStateFlow(0f)
    val magneticFieldStrength: StateFlow<Float> = _magneticFieldStrength.asStateFlow()

    private val _anomaliesDetected = MutableStateFlow(0)
    val anomaliesDetected: StateFlow<Int> = _anomaliesDetected.asStateFlow()

    init {
        viewModelScope.launch {
            magnetometerSensor.getSensorData().collect {
                _magneticFieldStrength.value = it.magnitude
                // Simple anomaly detection: if deviation is high
                val deviation = magnetometerSensor.getDeviation()
                val deviationMagnitude = kotlin.math.sqrt(
                    deviation.first * deviation.first +
                    deviation.second * deviation.second +
                    deviation.third * deviation.third
                )
                if (deviationMagnitude > 50) { // Threshold for anomaly
                    _anomaliesDetected.value = _anomaliesDetected.value + 1
                }
            }
        }

        viewModelScope.launch {
            accelerometerSensor.getSensorData().collect {
                // Can add anomaly detection for accelerometer here if needed
            }
        }
    }

    fun toggleScanning() {
        if (_isScanning.value) {
            stopScanning()
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        magnetometerSensor.startListening()
        accelerometerSensor.startListening()
        _isScanning.value = true
        _anomaliesDetected.value = 0 // Reset anomalies on new scan
        Timber.d("Scanning started")
    }

    private fun stopScanning() {
        magnetometerSensor.stopListening()
        accelerometerSensor.stopListening()
        _isScanning.value = false
        Timber.d("Scanning stopped")
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
        Timber.d("ScanViewModel cleared")
    }
}
