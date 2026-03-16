package com.sentinel.os.ui.screens

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.os.service.MeshNetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BroadcastViewModel(application: Application) : AndroidViewModel(application) {

    private val _nearbyNodes = MutableStateFlow<List<String>>(emptyList())
    val nearbyNodes: StateFlow<List<String>> = _nearbyNodes.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    init {
        // Start MeshNetworkService when ViewModel is created
        startMeshNetworkService(application)

        // TODO: Collect nearby nodes and messages from MeshNetworkService
        // This would involve binding to the service or using a broadcast receiver
        // For now, we'll simulate some data.
        viewModelScope.launch {
            // Simulate node discovery
            _nearbyNodes.value = listOf("Node-Alpha", "Node-Beta", "Node-Gamma")
        }
    }

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            _messages.value = _messages.value + "You: $message"
            // TODO: Send message via MeshNetworkService
            Timber.d("Sending message: $message")
        }
    }

    private fun startMeshNetworkService(context: Application) {
        val serviceIntent = Intent(context, MeshNetworkService::class.java)
        context.startForegroundService(serviceIntent)
        Timber.d("MeshNetworkService started from ViewModel")
    }

    private fun stopMeshNetworkService(context: Application) {
        val serviceIntent = Intent(context, MeshNetworkService::class.java)
        context.stopService(serviceIntent)
        Timber.d("MeshNetworkService stopped from ViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        stopMeshNetworkService(getApplication())
        Timber.d("BroadcastViewModel cleared")
    }
}
