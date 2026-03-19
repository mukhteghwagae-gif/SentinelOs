package com.sentinelos.app.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sentinelos.app.R
import com.sentinelos.app.ble.BleDevice
import com.sentinelos.app.ble.BleScanner
import com.sentinelos.app.utils.PermissionHelper

class BleFragment : Fragment() {

    private lateinit var scanner: BleScanner
    private lateinit var adapter: BleDeviceAdapter
    private lateinit var tvStatus: TextView
    private lateinit var tvCount: TextView
    private lateinit var btnScan: Button

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == android.app.Activity.RESULT_OK) scanner.startScan() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_ble, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanner = BleScanner(requireContext())
        adapter = BleDeviceAdapter(scanner)

        // IDs must match fragment_ble.xml
        tvStatus = view.findViewById(R.id.tv_ble_status)
        tvCount  = view.findViewById(R.id.tv_ble_count)
        btnScan  = view.findViewById(R.id.btn_ble_scan)

        view.findViewById<RecyclerView>(R.id.rv_ble_devices).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter  = this@BleFragment.adapter
        }

        setupScanner()

        btnScan.setOnClickListener {
            if (scanner.isScanning) scanner.stopScan() else checkAndStartScan()
        }
        view.findViewById<Button>(R.id.btn_ble_clear).setOnClickListener {
            scanner.clearDevices()
            adapter.notifyDataSetChanged()
            tvCount.text = "Devices: 0"
        }
    }

    private fun setupScanner() {
        if (!scanner.isBluetoothAvailable) {
            tvStatus.text = "Bluetooth not available on this device"
            btnScan.isEnabled = false
            return
        }
        scanner.onDeviceFound = { _ ->
            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
                tvCount.text = "Devices: ${scanner.devices.size}"
            }
        }
        scanner.onDeviceUpdated = { _ ->
            requireActivity().runOnUiThread { adapter.notifyDataSetChanged() }
        }
        scanner.onScanStateChanged = { scanning ->
            requireActivity().runOnUiThread {
                btnScan.text = if (scanning) "⏹ Stop Scan" else "▶ Start Scan"
                tvStatus.text = if (scanning) "Scanning…" else "Idle  •  ${scanner.devices.size} devices found"
            }
        }
        scanner.onScanError = { code ->
            requireActivity().runOnUiThread {
                val msg = when (code) {
                    -1 -> "Bluetooth is disabled — enable it first"
                    -2 -> "BLE scanner unavailable"
                    -3 -> "Permission denied — check app settings"
                    else -> "Scan error (code $code)"
                }
                tvStatus.text = msg
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndStartScan() {
        if (!PermissionHelper.hasBlePermissions(requireContext())) {
            PermissionHelper.requestPermissions(
                requireActivity(), PermissionHelper.blePermissions(), PermissionHelper.REQ_BLE_PERMISSIONS
            )
            return
        }
        if (!scanner.isBluetoothEnabled) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }
        scanner.startScan()
    }

    override fun onDestroyView() {
        scanner.stopScan()
        super.onDestroyView()
    }
}

// ─── RecyclerView Adapter ───────────────────────────────────────────────────

class BleDeviceAdapter(private val scanner: BleScanner) :
    RecyclerView.Adapter<BleDeviceAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        // IDs must match item_ble_device.xml
        val tvName:  TextView = view.findViewById(R.id.tv_ble_name)
        val tvAddr:  TextView = view.findViewById(R.id.tv_ble_addr)
        val tvRssi:  TextView = view.findViewById(R.id.tv_ble_rssi)
        val tvDist:  TextView = view.findViewById(R.id.tv_ble_dist)
        val tvExtra: TextView = view.findViewById(R.id.tv_ble_extra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_ble_device, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = scanner.devices[position]
        holder.tvName.text  = if (d.name.isNullOrBlank()) "Unknown Device" else d.name
        holder.tvAddr.text  = d.address
        val strength = scanner.getSignalStrengthLabel(d.rssi)
        val rssiColor = when (strength) {
            "Excellent" -> "#00E676"
            "Good"      -> "#69F0AE"
            "Fair"      -> "#FFD740"
            "Weak"      -> "#FF9100"
            else        -> "#FF1744"
        }
        holder.tvRssi.text  = "${d.rssi} dBm  •  $strength"
        holder.tvRssi.setTextColor(android.graphics.Color.parseColor(rssiColor))
        val dist = scanner.estimateDistance(d.rssi)
        holder.tvDist.text  = if (dist > 0) "~${"%.1f".format(dist)} m" else "—"
        val extras = buildList {
            d.manufacturer?.let { add("Mfr: $it") }
            d.txPower?.let      { add("TX: ${it} dBm") }
            add(if (d.isConnectable) "Connectable" else "Non-connectable")
            add("Seen ${d.scanCount}×")
        }
        holder.tvExtra.text = extras.joinToString("  •  ")
    }

    override fun getItemCount(): Int = scanner.devices.size
}
