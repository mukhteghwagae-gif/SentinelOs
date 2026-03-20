package com.sentinelos.app.ui

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.sentinelos.app.R
import com.sentinelos.app.services.NightGuardService
import com.sentinelos.app.services.SentinelService
import com.sentinelos.app.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    // Service references
    private var sentinelService: SentinelService? = null
    private var isSentinelBound = false

    // FIX #6: Safe binder cast using `is` check — avoids ClassCastException
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (service is SentinelService.LocalBinder) {
                sentinelService = service.getService()
                isSentinelBound = true
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            sentinelService = null
            isSentinelBound = false
        }
    }

    private val anomalyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val delta = intent.getFloatExtra("delta", 0f)
            showSnackbar("⚠️ Magnetic anomaly detected! Δ${"%.0f".format(delta)}µT")
        }
    }

    private val nightGuardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra("title") ?: "Alert"
            val detail = intent.getStringExtra("detail") ?: ""
            showSnackbar("🚨 Night Guard: $title — $detail")
        }
    }

    // FIX #1 + #3: Two-stage permission launcher
    // Stage 1 — all required except background location
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val denied = results.filterValues { !it }.keys
        if (denied.isEmpty()) {
            onStage1PermissionsGranted()
        } else {
            showPermissionRationale(denied.toList())
        }
    }

    // FIX #3: Stage 2 launcher — background location only
    private val bgLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Background location result — proceed regardless (it's optional)
        // Service was already started after stage 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "SentinelOS"

        setupBottomNav()

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Begin permission flow — stage 1
        requestAllPermissions()
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard   -> { loadFragment(DashboardFragment()); true }
                R.id.nav_sensors     -> { loadFragment(SensorsFragment()); true }
                R.id.nav_ble         -> { loadFragment(BleFragment()); true }
                R.id.nav_night_guard -> { loadFragment(NightGuardFragment()); true }
                R.id.nav_log         -> { loadFragment(LogFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestAllPermissions() {
        val perms = PermissionHelper.allRequiredPermissions()
        val needed = perms.filter { !PermissionHelper.hasPermission(this, it) }
        if (needed.isEmpty()) {
            onStage1PermissionsGranted()
        } else {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    // FIX #1: Gate service start on notification permission being granted
    // FIX #3: After stage 1, request background location as separate stage 2
    // FIX #4: Delay service start slightly via Handler.post to avoid race condition
    private fun onStage1PermissionsGranted() {
        registerBroadcastReceivers()

        if (PermissionHelper.hasCriticalPermissions(this)) {
            // FIX #4: Post to main looper — avoids OEM-specific race on cold launch
            Handler(Looper.getMainLooper()).post {
                startSentinelService()
            }
        } else {
            // Notification permission was denied — service cannot start safely
            showNotificationPermissionWarning()
        }

        // FIX #3: Stage 2 — request background location separately, only if fine location granted
        if (PermissionHelper.hasLocationPermission(this) &&
            PermissionHelper.needsBackgroundLocation() &&
            !PermissionHelper.hasBackgroundLocationPermission(this)
        ) {
            // Brief delay so system dialog doesn't stack on top of stage 1 dialog
            Handler(Looper.getMainLooper()).postDelayed({
                bgLocationLauncher.launch(PermissionHelper.backgroundLocationPermission())
            }, 800)
        }
    }

    private fun showPermissionRationale(denied: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage(
                "SentinelOS needs the following permissions to function:\n\n" +
                denied.joinToString("\n") { "• ${it.substringAfterLast(".")}" } +
                "\n\nSome features will be limited without them."
            )
            .setPositiveButton("Grant") { _, _ -> requestAllPermissions() }
            .setNegativeButton("Open Settings") { _, _ -> PermissionHelper.openAppSettings(this) }
            .setNeutralButton("Continue Anyway") { _, _ ->
                registerBroadcastReceivers()
                // Only start service if critical permissions are met
                if (PermissionHelper.hasCriticalPermissions(this)) {
                    Handler(Looper.getMainLooper()).post { startSentinelService() }
                }
                loadFragment(DashboardFragment())
            }
            .show()
    }

    private fun showNotificationPermissionWarning() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Notification permission required for background monitoring",
            Snackbar.LENGTH_LONG
        ).setAction("Grant") {
            PermissionHelper.openAppSettings(this)
        }.show()
    }

    private fun startSentinelService() {
        // FIX #1: Final guard — never start foreground service without notification permission
        if (!PermissionHelper.hasNotificationPermission(this)) {
            showNotificationPermissionWarning()
            return
        }

        val intent = Intent(this, SentinelService::class.java).apply {
            action = SentinelService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        // Bind for IPC
        bindService(
            Intent(this, SentinelService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    fun startNightGuard(sensitivity: Float = 1.5f) {
        // FIX #1: Guard Night Guard service start too
        if (!PermissionHelper.hasNotificationPermission(this)) {
            showNotificationPermissionWarning()
            return
        }
        val intent = Intent(this, NightGuardService::class.java).apply {
            action = NightGuardService.ACTION_START
            putExtra("sensitivity", sensitivity)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Night Guard activated", Toast.LENGTH_SHORT).show()
    }

    fun stopNightGuard() {
        stopService(Intent(this, NightGuardService::class.java))
        Toast.makeText(this, "Night Guard deactivated", Toast.LENGTH_SHORT).show()
    }

    private fun registerBroadcastReceivers() {
        val filter1 = IntentFilter(SentinelService.BROADCAST_ANOMALY)
        val filter2 = IntentFilter(NightGuardService.BROADCAST_ALERT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(anomalyReceiver, filter1, RECEIVER_NOT_EXPORTED)
            registerReceiver(nightGuardReceiver, filter2, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(anomalyReceiver, filter1)
            registerReceiver(nightGuardReceiver, filter2)
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("SentinelOS v2.0")
            .setMessage(
                "Security & Sensor Intelligence Platform\n\n" +
                "• Magnetometer anomaly detection\n" +
                "• BLE device scanning\n" +
                "• Night guard motion alerts\n" +
                "• Multi-sensor monitoring\n\n" +
                "Built for real-world security."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        if (isSentinelBound) {
            unbindService(serviceConnection)
            isSentinelBound = false
        }
        try {
            unregisterReceiver(anomalyReceiver)
            unregisterReceiver(nightGuardReceiver)
        } catch (e: Exception) {
            // Receivers may not be registered if permissions were never granted
        }
        super.onDestroy()
    }
}
