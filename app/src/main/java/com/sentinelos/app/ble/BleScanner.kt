package com.sentinelos.app.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build

data class BleDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    val txPower: Int?,
    val isConnectable: Boolean,
    val manufacturer: String?,
    var lastSeen: Long = System.currentTimeMillis(),
    var scanCount: Int = 1
)

class BleScanner(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var leScanner: BluetoothLeScanner? = null

    val isBluetoothAvailable: Boolean get() = bluetoothAdapter != null
    val isBluetoothEnabled: Boolean get() = bluetoothAdapter?.isEnabled == true

    private val _devices = mutableMapOf<String, BleDevice>()
    val devices: List<BleDevice> get() = _devices.values.sortedByDescending { it.rssi }

    var onDeviceFound: ((BleDevice) -> Unit)? = null
    var onDeviceUpdated: ((BleDevice) -> Unit)? = null
    var onScanError: ((Int) -> Unit)? = null
    var onScanStateChanged: ((Boolean) -> Unit)? = null

    var isScanning = false
        private set

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            processScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { processScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            onScanStateChanged?.invoke(false)
            onScanError?.invoke(errorCode)
        }
    }

    private fun processScanResult(result: ScanResult) {
        val addr = result.device.address
        val name = try { result.device.name } catch (e: SecurityException) { null }
        val manufacturer = result.scanRecord?.manufacturerSpecificData?.let { data ->
            if (data.size() > 0) "0x%04X".format(data.keyAt(0)) else null
        }
        val txPower = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result.txPower.takeIf { it != ScanResult.TX_POWER_NOT_PRESENT }
        } else null
        val isConnectable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result.isConnectable
        } else true

        val existing = _devices[addr]
        if (existing != null) {
            existing.lastSeen = System.currentTimeMillis()
            existing.scanCount++
            onDeviceUpdated?.invoke(existing)
        } else {
            val device = BleDevice(
                address = addr,
                name = name,
                rssi = result.rssi,
                txPower = txPower,
                isConnectable = isConnectable,
                manufacturer = manufacturer
            )
            _devices[addr] = device
            onDeviceFound?.invoke(device)
        }
    }

    /** Start BLE scan — call only after checking hasBlePermissions() */
    fun startScan() {
        if (!isBluetoothAvailable || !isBluetoothEnabled) {
            onScanError?.invoke(-1)
            return
        }
        if (isScanning) return

        leScanner = bluetoothAdapter!!.bluetoothLeScanner
        if (leScanner == null) {
            onScanError?.invoke(-2)
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            leScanner!!.startScan(emptyList<ScanFilter>(), settings, scanCallback)
            isScanning = true
            onScanStateChanged?.invoke(true)
        } catch (e: SecurityException) {
            onScanError?.invoke(-3)
        } catch (e: Exception) {
            onScanError?.invoke(-4)
        }
    }

    fun stopScan() {
        if (!isScanning) return
        try {
            leScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Ignore
        } catch (e: Exception) {
            // Ignore
        } finally {
            isScanning = false
            leScanner = null
            onScanStateChanged?.invoke(false)
        }
    }

    fun clearDevices() {
        _devices.clear()
    }

    /** Estimate distance from RSSI */
    fun estimateDistance(rssi: Int, txPower: Int = -59): Double {
        if (rssi == 0) return -1.0
        val ratio = rssi * 1.0 / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            0.89976 * Math.pow(ratio, 7.7095) + 0.111
        }
    }

    fun getSignalStrengthLabel(rssi: Int): String = when {
        rssi >= -50 -> "Excellent"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        rssi >= -80 -> "Weak"
        else -> "Very Weak"
    }
}
