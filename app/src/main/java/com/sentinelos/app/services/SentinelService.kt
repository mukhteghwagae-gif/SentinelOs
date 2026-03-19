package com.sentinelos.app.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentinelos.app.R
import com.sentinelos.app.SentinelApp
import com.sentinelos.app.sensors.MagnetometerManager
import com.sentinelos.app.ui.MainActivity

class SentinelService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): SentinelService = this@SentinelService
    }

    private val binder = LocalBinder()
    private lateinit var magnetometerManager: MagnetometerManager
    private var anomalyCount = 0

    override fun onCreate() {
        super.onCreate()
        magnetometerManager = MagnetometerManager(this)
        setupSensorCallbacks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun setupSensorCallbacks() {
        magnetometerManager.onAnomalyDetected = { magnitude, baseline, delta ->
            anomalyCount++
            updateNotification("⚠️ Magnetic anomaly! Δ${"%.0f".format(delta)}µT")
            broadcastAnomaly(magnitude, baseline, delta)
        }
        magnetometerManager.onMagneticUpdate = { _, _, _, strength ->
            updateNotification("Monitoring | Field: ${"%.1f".format(strength)}µT")
        }
    }

    private fun startMonitoring() {
        val notification = buildNotification("Sentinel active — monitoring sensors")
        startForeground(NOTIFICATION_ID, notification)
        magnetometerManager.startListening()
    }

    private fun stopMonitoring() {
        magnetometerManager.stopListening()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SentinelService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SentinelApp.CHANNEL_SENTINEL)
            .setContentTitle("SentinelOS")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun broadcastAnomaly(magnitude: Float, baseline: Float, delta: Float) {
        val intent = Intent(BROADCAST_ANOMALY).apply {
            putExtra("magnitude", magnitude)
            putExtra("baseline", baseline)
            putExtra("delta", delta)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ACTION_START_SENTINEL"
        const val ACTION_STOP = "ACTION_STOP_SENTINEL"
        const val BROADCAST_ANOMALY = "com.sentinelos.ANOMALY"
        const val NOTIFICATION_ID = 1001
    }
}
