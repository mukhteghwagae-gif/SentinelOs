package com.sentinelos.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only auto-start if user had it enabled (check SharedPrefs)
            val prefs = context.getSharedPreferences("sentinel_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("auto_start_on_boot", false)) {
                val serviceIntent = Intent(context, SentinelService::class.java).apply {
                    action = SentinelService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
