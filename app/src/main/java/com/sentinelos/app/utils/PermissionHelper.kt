package com.sentinelos.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionHelper — Fixed v2
 *
 * Fixes applied:
 *  1. POST_NOTIFICATIONS explicitly included for API 33+
 *  2. SYSTEM_ALERT_WINDOW removed from runtime request list
 *  3. ACCESS_BACKGROUND_LOCATION separated into stage-2
 *  4. hasOverlayPermission() / openOverlaySettings() added
 *  5. hasCriticalPermissions() gate for service start
 */
object PermissionHelper {

    // STAGE 1 — request on launch. No BACKGROUND_LOCATION, no SYSTEM_ALERT_WINDOW
    fun allRequiredPermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
        // FIX #1: POST_NOTIFICATIONS is a runtime permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        // ACCESS_BACKGROUND_LOCATION intentionally excluded — use requestBackgroundLocation()
        // SYSTEM_ALERT_WINDOW intentionally excluded — use openOverlaySettings()
        return perms.toTypedArray()
    }

    // STAGE 2 — background location, only after fine location is already granted
    fun backgroundLocationPermission(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            emptyArray()
        }

    fun needsBackgroundLocation(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun blePermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun hasPermission(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasBlePermissions(context: Context): Boolean =
        blePermissions().all { hasPermission(context, it) }

    fun hasLocationPermission(context: Context): Boolean =
        hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun hasCameraPermission(context: Context): Boolean =
        hasPermission(context, Manifest.permission.CAMERA)

    // FIX #1 — always call this before startForegroundService()
    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    }

    // FIX #2 — SYSTEM_ALERT_WINDOW requires Settings API, NOT PackageManager
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    // Gate: both notification and location must be granted before starting service
    fun hasCriticalPermissions(context: Context): Boolean =
        hasNotificationPermission(context) && hasLocationPermission(context)

    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // FIX #2 — dedicated settings screen for overlay permission
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }

    fun allGranted(grantResults: IntArray): Boolean =
        grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

    const val REQ_ALL_PERMISSIONS         = 1001
    const val REQ_BLE_PERMISSIONS         = 1002
    const val REQ_LOCATION_PERMISSIONS    = 1003
    const val REQ_CAMERA_PERMISSION       = 1004
    const val REQ_NOTIFICATION_PERMISSION = 1005
    const val REQ_BG_LOCATION_PERMISSION  = 1006
}
