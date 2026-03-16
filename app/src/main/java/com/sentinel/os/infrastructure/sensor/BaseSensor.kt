package com.sentinel.os.infrastructure.sensor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base class for all sensor implementations.
 * Provides common interface for sensor data streaming using Kotlin Flow.
 */
abstract class BaseSensor<T> {
    protected val _sensorData = MutableStateFlow<T?>(null)
    val sensorData: StateFlow<T?> = _sensorData.asStateFlow()

    protected val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    abstract fun startListening()
    abstract fun stopListening()
    abstract fun getSensorData(): Flow<T>

    protected fun updateData(data: T) {
        _sensorData.value = data
    }

    protected fun setError(message: String) {
        _error.value = message
    }

    protected fun clearError() {
        _error.value = null
    }
}
