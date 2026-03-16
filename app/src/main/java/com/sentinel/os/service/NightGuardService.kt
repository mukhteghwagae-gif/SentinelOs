package com.sentinel.os.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.media.MediaPlayer
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.sentinel.os.R
import com.sentinel.os.domain.usecase.ThreatFusionEngine
import com.sentinel.os.infrastructure.sensor.AccelerometerSensor
import com.sentinel.os.infrastructure.sensor.MagnetometerSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Foreground service for Night Guard mode.
 * Continuously monitors all sensors and triggers alerts based on threat fusion engine.
 */
class NightGuardService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var threatFusionEngine: ThreatFusionEngine
    private lateinit var magnetometerSensor: MagnetometerSensor
    private lateinit var accelerometerSensor: AccelerometerSensor

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "night_guard_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("NightGuardService created")

        threatFusionEngine = ThreatFusionEngine()
        magnetometerSensor = MagnetometerSensor(this)
        accelerometerSensor = AccelerometerSensor(this)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("NightGuardService started")

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        startMonitoring()

        return START_STICKY
    }

    private fun startMonitoring() {
        magnetometerSensor.startListening()
        accelerometerSensor.startListening()

        serviceScope.launch {
            magnetometerSensor.getSensorData().collect { data ->
                val deviation = magnetometerSensor.getDeviation()
                val deviationMagnitude = kotlin.math.sqrt(
                    deviation.first * deviation.first +
                    deviation.second * deviation.second +
                    deviation.third * deviation.third
                )
                threatFusionEngine.updateMagneticScore(deviationMagnitude * 10)
            }
        }

        serviceScope.launch {
            accelerometerSensor.getSensorData().collect { data ->
                threatFusionEngine.updateVibrationScore(data.magnitude * 5)
            }
        }

        serviceScope.launch {
            threatFusionEngine.threatAssessment.collect { assessment ->
                assessment?.let {
                    if (it.threatScore > 60) {
                        triggerAlert(it.threatScore, it.threatLevel)
                    }
                    updateNotification(it.threatScore, it.threatLevel)
                }
            }
        }
    }

    private fun triggerAlert(score: Float, level: String) {
        Timber.w("ALERT TRIGGERED: Threat Level $level (Score: $score)")

        // Play alarm sound
        try {
            val alarm = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
            alarm.isLooping = false
            alarm.start()
        } catch (e: Exception) {
            Timber.e(e, "Error playing alarm sound")
        }

        // Send enhanced alert notification
        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CRITICAL ALERT: Threat Detected!")
            .setContentText("Threat Level: $level (Score: ${"%.1f".format(score)}). Tap for details.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, alertNotification) // Use a different ID for alert notification

        // TODO: Implement video/audio recording (Subscription limitation for video generation)
        Timber.i("Video/audio recording feature requires subscription upgrade.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Night Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Night Guard monitoring active"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name) + " - Night Guard Active")
            .setContentText("Monitoring for threats...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(score: Float, level: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name) + " - Night Guard Active")
            .setContentText("Threat Level: $level (Score: ${"%.1f".format(score)})")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        magnetometerSensor.stopListening()
        accelerometerSensor.stopListening()
        serviceScope.cancel()
        Timber.d("NightGuardService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
