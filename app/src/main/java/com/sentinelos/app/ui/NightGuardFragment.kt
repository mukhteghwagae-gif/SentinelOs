package com.sentinelos.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sentinelos.app.R
import com.sentinelos.app.services.NightGuardService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NightGuardFragment : Fragment() {

    // IDs verified against fragment_night_guard.xml
    private lateinit var switchNightGuard:   Switch
    private lateinit var seekSensitivity:    SeekBar
    private lateinit var tvSensitivityLabel: TextView
    private lateinit var tvGuardStatus:      TextView
    private lateinit var tvAlertLog:         TextView
    private lateinit var btnClearLog:        Button
    private lateinit var tvAlertCount:       TextView

    private var alertCount = 0
    private val logBuilder = StringBuilder()
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var isGuardActive = false

    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            alertCount++
            val title  = intent.getStringExtra("title")  ?: "Alert"
            val detail = intent.getStringExtra("detail") ?: ""
            val time   = sdf.format(Date())
            logBuilder.insert(0, "[$time] 🚨 $title: $detail\n")
            activity?.runOnUiThread {
                tvAlertCount.text = "Total alerts: $alertCount"
                tvAlertLog.text   = logBuilder.toString().take(3000)
                tvGuardStatus.setTextColor(Color.parseColor("#FF1744"))
                tvGuardStatus.text = "🚨 ALERT TRIGGERED — Night Guard Active"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_night_guard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchNightGuard   = view.findViewById(R.id.switch_night_guard)
        seekSensitivity    = view.findViewById(R.id.seek_sensitivity)
        tvSensitivityLabel = view.findViewById(R.id.tv_sensitivity_label)
        tvGuardStatus      = view.findViewById(R.id.tv_guard_status)
        tvAlertLog         = view.findViewById(R.id.tv_alert_log)
        btnClearLog        = view.findViewById(R.id.btn_clear_log)
        tvAlertCount       = view.findViewById(R.id.tv_night_alert_count)

        seekSensitivity.max      = 40
        seekSensitivity.progress = 10
        updateSensitivityLabel(10)

        seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                updateSensitivityLabel(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        switchNightGuard.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            if (checked) activateNightGuard() else deactivateNightGuard()
        }

        btnClearLog.setOnClickListener {
            logBuilder.clear()
            alertCount = 0
            tvAlertLog.text   = "No alerts yet."
            tvAlertCount.text = "Total alerts: 0"
        }

        tvGuardStatus.text = "Night Guard: Inactive"
        tvAlertLog.text    = "No alerts yet."
    }

    private fun updateSensitivityLabel(progress: Int) {
        val sensitivity = 0.5f + progress * 0.1f
        val label = when {
            sensitivity < 1.0f -> "Very High — hair-trigger"
            sensitivity < 2.0f -> "High"
            sensitivity < 3.0f -> "Medium"
            sensitivity < 4.0f -> "Low"
            else               -> "Very Low — coarse"
        }
        tvSensitivityLabel.text = "Sensitivity: ${"%.1f".format(sensitivity)} m/s² — $label"
    }

    private fun activateNightGuard() {
        val sensitivity = 0.5f + seekSensitivity.progress * 0.1f
        (activity as? MainActivity)?.startNightGuard(sensitivity)
        isGuardActive = true
        tvGuardStatus.text = "🌙 Night Guard: ACTIVE — auto-calibrating (3 s)…"
        tvGuardStatus.setTextColor(Color.parseColor("#00E676"))
        seekSensitivity.isEnabled = false
    }

    private fun deactivateNightGuard() {
        (activity as? MainActivity)?.stopNightGuard()
        isGuardActive = false
        tvGuardStatus.text = "Night Guard: Inactive"
        tvGuardStatus.setTextColor(Color.parseColor("#757575"))
        seekSensitivity.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(NightGuardService.BROADCAST_ALERT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireContext().registerReceiver(alertReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else requireContext().registerReceiver(alertReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        try { requireContext().unregisterReceiver(alertReceiver) } catch (e: Exception) {}
    }
}
