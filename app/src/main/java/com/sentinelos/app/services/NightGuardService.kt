package com.sentinelos.app.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sentinelos.app.SentinelApp
import com.sentinelos.app.ui.MainActivity
import kotlin.math.abs
import kotlin.math.sqrt

class NightGuardService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var magneticSensor: Sensor? = null
    private var proximitySensor: Sensor? = null

    // Baseline values for motion detection
    private var baselineAccel = FloatArray(3) { 0f }
    private var lastAccel = FloatArray(3) { 0f }
    private var calibrationSamples = 0
    private var isCalibrated = false

    // Night guard config
    var motionSensitivity: Float = 1.5f   // m/s² delta to trigger
    var magneticSensitivity: Float = 40f  // µT delta to trigger
    private var baselineMagnetic: Float = 0f
    private var lastMagnetic: Float = 0f

    private val alertCooldownMs = 3000L
    private var lastAlertTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var alertCount = 0

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                motionSensitivity = intent.getFloatExtra("sensitivity", 1.5f)
                startNightGuard()
            }
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startNightGuard() {
        startForeground(NOTIFICATION_ID, buildNotification("Night Guard: Calibrating..."))
        registerSensors()
        // Auto-calibrate after 3 seconds of settling
        handler.postDelayed({
            calibrate()
            updateNotification("Night Guard ACTIVE | 0 alerts")
        }, 3000)
    }

    private fun registerSensors() {
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magneticSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun calibrate() {
        baselineAccel = lastAccel.copyOf()
        baselineMagnetic = lastMagnetic
        isCalibrated = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAccel = event.values.copyOf()
                if (!isCalibrated) return

                val dx = abs(event.values[0] - baselineAccel[0])
                val dy = abs(event.values[1] - baselineAccel[1])
                val dz = abs(event.values[2] - baselineAccel[2])
                val delta = sqrt(dx * dx + dy * dy + dz * dz)

                if (delta > motionSensitivity) {
                    triggerAlert("Motion detected", "Movement: ${"%.1f".format(delta)} m/s²")
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lastMagnetic = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                if (!isCalibrated) return
                val delta = abs(lastMagnetic - baselineMagnetic)
                if (delta > magneticSensitivity) {
                    triggerAlert("Magnetic anomaly", "Field change: ${"%.0f".format(delta)}µT")
                }
            }
        }
    }

    private fun triggerAlert(title: String, detail: String) {
        val now = System.currentTimeMillis()
        if (now - lastAlertTime < alertCooldownMs) return
        lastAlertTime = now
        alertCount++

        // Vibrate
        vibrate()
        // Update notification
        updateNotification("🚨 ALERT: $title | Total: $alertCount")
        // Send broadcast to UI
        sendBroadcast(Intent(BROADCAST_ALERT).apply {
            putExtra("title", title)
            putExtra("detail", detail)
            putExtra("count", alertCount)
            putExtra("timestamp", now)
        })
        // High-priority notification
        val nm = getSystemService(NotificationManager::class.java)
        val alertNotif = NotificationCompat.Builder(this, SentinelApp.CHANNEL_ALERTS)
            .setContentTitle("🚨 SentinelOS Alert")
            .setContentText("$title: $detail")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(ALERT_NOTIFICATION_ID + alertCount, alertNotif)
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(VibratorManager::class.java)
                vm.defaultVibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 200, 100, 200), -1)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration failures
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, NightGuardService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SentinelApp.CHANNEL_NIGHT_GUARD)
            .setContentTitle("🌙 Night Guard")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pi)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Deactivate", stopPi)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ACTION_START_NIGHT_GUARD"
        const val ACTION_STOP = "ACTION_STOP_NIGHT_GUARD"
        const val BROADCAST_ALERT = "com.sentinelos.NIGHT_GUARD_ALERT"
        const val NOTIFICATION_ID = 1002
        const val ALERT_NOTIFICATION_ID = 2000
    }
}
