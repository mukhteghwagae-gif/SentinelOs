package com.sentinelos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SentinelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Sentinel monitoring channel
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SENTINEL,
                    "Sentinel Monitoring",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Background sensor monitoring service"
                    setShowBadge(false)
                }
            )

            // Night guard channel
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_NIGHT_GUARD,
                    "Night Guard",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Night guard alerts and status"
                }
            )

            // Alerts channel
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ALERTS,
                    "Security Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Intrusion and anomaly alerts"
                    enableVibration(true)
                }
            )
        }
    }

    companion object {
        const val CHANNEL_SENTINEL = "channel_sentinel"
        const val CHANNEL_NIGHT_GUARD = "channel_night_guard"
        const val CHANNEL_ALERTS = "channel_alerts"
    }
}
