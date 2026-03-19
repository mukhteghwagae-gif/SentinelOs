package com.sentinelos.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sentinelos.app.R

class BleScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_scan)
        supportActionBar?.apply { title = "BLE Scanner"; setDisplayHomeAsUpEnabled(true) }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
