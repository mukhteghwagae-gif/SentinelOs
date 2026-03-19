package com.sentinelos.app.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sentinelos.app.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        val prefs = getSharedPreferences("sentinel_prefs", Context.MODE_PRIVATE)

        val switchAutoStart = findViewById<Switch>(R.id.switch_auto_start)
        switchAutoStart.isChecked = prefs.getBoolean("auto_start_on_boot", false)
        switchAutoStart.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_start_on_boot", checked).apply()
        }

        val seekThreshold = findViewById<SeekBar>(R.id.seek_mag_threshold)
        val tvThreshold = findViewById<TextView>(R.id.tv_mag_threshold)
        val savedThreshold = prefs.getInt("mag_threshold", 50)
        seekThreshold.progress = savedThreshold
        tvThreshold.text = "Magnetic anomaly threshold: $savedThreshold µT"
        seekThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                val t = maxOf(10, progress)
                tvThreshold.text = "Magnetic anomaly threshold: $t µT"
                prefs.edit().putInt("mag_threshold", t).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.btn_open_permissions).setOnClickListener {
            com.sentinelos.app.utils.PermissionHelper.openAppSettings(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
