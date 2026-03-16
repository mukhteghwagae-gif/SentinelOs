package com.sentinel.os.ui.screens

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.os.domain.usecase.ThreatFusionEngine
import com.sentinel.os.domain.usecase.ThreatAssessment
import com.sentinel.os.service.NightGuardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class GuardViewModel(application: Application) : AndroidViewModel(application) {

    private val _isGuardActive = MutableStateFlow(false)
    val isGuardActive: StateFlow<Boolean> = _isGuardActive.asStateFlow()

    private val _threatAssessment = MutableStateFlow<ThreatAssessment?>(null)
    val threatAssessment: StateFlow<ThreatAssessment?> = _threatAssessment.asStateFlow()

    private val threatFusionEngine = ThreatFusionEngine()

    init {
        viewModelScope.launch {
            threatFusionEngine.threatAssessment.collect {
                _threatAssessment.value = it
            }
        }
    }

    fun toggleGuardService(context: Application) {
        if (_isGuardActive.value) {
            stopGuardService(context)
        } else {
            startGuardService(context)
        }
    }

    private fun startGuardService(context: Application) {
        val serviceIntent = Intent(context, NightGuardService::class.java)
        context.startForegroundService(serviceIntent)
        _isGuardActive.value = true
        Timber.d("NightGuardService started from ViewModel")
    }

    private fun stopGuardService(context: Application) {
        val serviceIntent = Intent(context, NightGuardService::class.java)
        context.stopService(serviceIntent)
        _isGuardActive.value = false
        Timber.d("NightGuardService stopped from ViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure service is stopped when ViewModel is cleared, if it's still active
        if (_isGuardActive.value) {
            stopGuardService(getApplication())
        }
        Timber.d("GuardViewModel cleared")
    }
}
