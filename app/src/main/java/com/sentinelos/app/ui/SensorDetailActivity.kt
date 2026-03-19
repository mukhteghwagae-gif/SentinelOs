package com.sentinelos.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sentinelos.app.R

class SensorDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_detail)
        supportActionBar?.apply { title = "Sensor Detail"; setDisplayHomeAsUpEnabled(true) }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
