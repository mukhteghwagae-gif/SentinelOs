package com.sentinel.os.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentinel.os.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber

/**
 * Foreground service for EchoNet mesh networking.
 * Handles WiFi Direct discovery, BLE beacon broadcasting, and message routing.
 */
class MeshNetworkService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var wifiManager: WifiManager

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "mesh_network_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("MeshNetworkService created")

        wifiManager = getSystemService(WifiManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("MeshNetworkService started")

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        startMeshNetworking()

        return START_STICKY
    }

    private fun startMeshNetworking() {
        Timber.d("Starting mesh networking")
        // TODO: Implement WiFi Direct peer discovery
        // TODO: Implement BLE beacon broadcasting
        // TODO: Implement message routing and store-forward logic
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EchoNet Mesh",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mesh networking active"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EchoNet Mesh Active")
            .setContentText("Discovering nearby nodes...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.d("MeshNetworkService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
