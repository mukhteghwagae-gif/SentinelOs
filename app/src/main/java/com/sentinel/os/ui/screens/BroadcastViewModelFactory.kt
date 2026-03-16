package com.sentinel.os.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BroadcastViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BroadcastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BroadcastViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
