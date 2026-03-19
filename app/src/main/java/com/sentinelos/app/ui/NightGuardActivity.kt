package com.sentinelos.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sentinelos.app.R

class NightGuardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_night_guard)
        supportActionBar?.apply { title = "Night Guard"; setDisplayHomeAsUpEnabled(true) }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
