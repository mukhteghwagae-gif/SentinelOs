package com.sentinelos.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.sentinelos.app.R
import com.sentinelos.app.sensors.MagnetometerManager
import com.sentinelos.app.services.NightGuardService
import com.sentinelos.app.services.SentinelService

class DashboardFragment : Fragment() {

    private lateinit var magnetometerManager: MagnetometerManager
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // IDs verified against fragment_dashboard.xml
    private lateinit var tvMagField:    TextView
    private lateinit var tvAccel:       TextView
    private lateinit var tvAzimuth:     TextView
    private lateinit var tvLight:       TextView
    private lateinit var tvProximity:   TextView
    private lateinit var tvGyro:        TextView
    private lateinit var tvAlertCount:  TextView
    private lateinit var tvCalibStatus: TextView
    private lateinit var cardMag:       CardView
    private lateinit var cardAlert:     CardView
    private var alertCount = 0

    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            alertCount++
            tvAlertCount.text = "Alerts today: $alertCount"
            cardAlert.setCardBackgroundColor(Color.parseColor("#B71C1C"))
            handler.postDelayed({
                if (isAdded) cardAlert.setCardBackgroundColor(Color.parseColor("#16161A"))
            }, 2500)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        magnetometerManager = MagnetometerManager(requireContext())
        magnetometerManager.startListening()
        startPeriodicUpdate()

        val filter = IntentFilter(NightGuardService.BROADCAST_ALERT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireContext().registerReceiver(alertReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else requireContext().registerReceiver(alertReceiver, filter)
    }

    private fun bindViews(v: View) {
        tvMagField    = v.findViewById(R.id.tv_mag_field)
        tvAccel       = v.findViewById(R.id.tv_accel)
        tvAzimuth     = v.findViewById(R.id.tv_azimuth)
        tvLight       = v.findViewById(R.id.tv_light)
        tvProximity   = v.findViewById(R.id.tv_proximity)
        tvGyro        = v.findViewById(R.id.tv_gyro)
        tvAlertCount  = v.findViewById(R.id.tv_alert_count)
        tvCalibStatus = v.findViewById(R.id.tv_calib_status)
        cardMag       = v.findViewById(R.id.card_magnetic)
        cardAlert     = v.findViewById(R.id.card_alerts)

        v.findViewById<Button>(R.id.btn_calibrate).setOnClickListener {
            magnetometerManager.calibrate()
            tvCalibStatus.text =
                "Calibrated ✓  baseline: ${"%.1f".format(magnetometerManager.baselineMagnetic)} µT"
        }
        v.findViewById<Button>(R.id.btn_go_sensors).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SensorsFragment()).addToBackStack(null).commit()
        }
        v.findViewById<Button>(R.id.btn_go_ble).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BleFragment()).addToBackStack(null).commit()
        }
    }

    private fun startPeriodicUpdate() {
        updateRunnable = object : Runnable {
            override fun run() { if (isAdded) updateUI(); handler.postDelayed(this, 500) }
        }
        handler.post(updateRunnable!!)
    }

    private fun updateUI() {
        val mag = magnetometerManager.magneticStrength
        tvMagField.text = "${"%.1f".format(mag)} µT"
        val magColor = when {
            mag > 100 -> Color.parseColor("#B71C1C")
            mag > 70  -> Color.parseColor("#E65100")
            else      -> Color.parseColor("#16161A")
        }
        cardMag.setCardBackgroundColor(magColor)

        val a = magnetometerManager.acceleration
        tvAccel.text = "${"%.1f".format(a[0])}, ${"%.1f".format(a[1])}, ${"%.1f".format(a[2])} m/s²"

        tvAzimuth.text   = "${"%.0f".format(magnetometerManager.azimuth)}° ${compassDir(magnetometerManager.azimuth)}"
        val lux = magnetometerManager.lightLevel
        tvLight.text     = if (lux >= 0) "${"%.0f".format(lux)} lux" else "N/A"
        val prox = magnetometerManager.proximity
        tvProximity.text = if (prox >= 0) "${"%.1f".format(prox)} cm" else "N/A"
        val g = magnetometerManager.gyroscope
        tvGyro.text      = "${"%.2f".format(g[0])}, ${"%.2f".format(g[1])}, ${"%.2f".format(g[2])} rad/s"
    }

    private fun compassDir(az: Float) = when {
        az < 22.5 || az >= 337.5 -> "N"
        az < 67.5  -> "NE"; az < 112.5 -> "E"; az < 157.5 -> "SE"
        az < 202.5 -> "S"; az < 247.5  -> "SW"; az < 292.5 -> "W"
        else -> "NW"
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        magnetometerManager.stopListening()
        try { requireContext().unregisterReceiver(alertReceiver) } catch (e: Exception) {}
        super.onDestroyView()
    }
}
