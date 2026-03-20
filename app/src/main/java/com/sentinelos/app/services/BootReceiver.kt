package com.sentinelos.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sentinelos.app.utils.PermissionHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("sentinel_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("auto_start_on_boot", false)) return

            // FIX #1: Do not start foreground service on boot without notification permission
            if (!PermissionHelper.hasCriticalPermissions(context)) return

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
