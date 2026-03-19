package com.sentinelos.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sentinelos.app.R
import com.sentinelos.app.services.NightGuardService
import com.sentinelos.app.services.SentinelService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogFragment : Fragment() {

    // IDs verified against fragment_log.xml
    private lateinit var tvLog:        TextView
    private lateinit var scrollView:   ScrollView
    private lateinit var btnClear:     Button
    private lateinit var tvEventCount: TextView

    private val log         = StringBuilder()
    private var eventCount  = 0
    private val sdf         = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private fun timestamp() = sdf.format(Date())

    private val combinedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                SentinelService.BROADCAST_ANOMALY -> {
                    val delta     = intent.getFloatExtra("delta", 0f)
                    val magnitude = intent.getFloatExtra("magnitude", 0f)
                    appendLog("⚠️ MAGNETIC ANOMALY | Δ${"%.1f".format(delta)} µT  |  Total ${"%.1f".format(magnitude)} µT")
                }
                NightGuardService.BROADCAST_ALERT -> {
                    val title  = intent.getStringExtra("title")  ?: "Alert"
                    val detail = intent.getStringExtra("detail") ?: ""
                    appendLog("🚨 NIGHT GUARD | $title | $detail")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvLog        = view.findViewById(R.id.tv_log)
        scrollView   = view.findViewById(R.id.scroll_log)
        btnClear     = view.findViewById(R.id.btn_clear_log_main)
        tvEventCount = view.findViewById(R.id.tv_event_count)

        btnClear.setOnClickListener {
            log.clear()
            eventCount = 0
            tvLog.text        = "Log cleared. Waiting for events…"
            tvEventCount.text = "0 events"
        }

        appendLog("ℹ️ SentinelOS started")
        appendLog("ℹ️ Sensors initialised — monitoring active")
    }

    private fun appendLog(message: String) {
        eventCount++
        log.insert(0, "[${timestamp()}]  $message\n\n")
        activity?.runOnUiThread {
            tvLog.text        = log.toString().take(6000)
            tvEventCount.text = "$eventCount events"
            scrollView.post { scrollView.scrollTo(0, 0) }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(SentinelService.BROADCAST_ANOMALY)
            addAction(NightGuardService.BROADCAST_ALERT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireContext().registerReceiver(combinedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else requireContext().registerReceiver(combinedReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        try { requireContext().unregisterReceiver(combinedReceiver) } catch (e: Exception) {}
    }
}
