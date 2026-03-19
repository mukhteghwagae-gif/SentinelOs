package com.sentinelos.app.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sentinelos.app.R
import com.sentinelos.app.sensors.MagnetometerManager
import kotlin.math.min

class SensorsFragment : Fragment() {

    private lateinit var mag: MagnetometerManager
    private val handler = Handler(Looper.getMainLooper())

    // All IDs verified against fragment_sensors.xml
    private lateinit var tvMagX: TextView
    private lateinit var tvMagY: TextView
    private lateinit var tvMagZ: TextView
    private lateinit var tvMagTotal: TextView
    private lateinit var pbMagStrength: ProgressBar
    private lateinit var tvAccX: TextView
    private lateinit var tvAccY: TextView
    private lateinit var tvAccZ: TextView
    private lateinit var tvGyroX: TextView
    private lateinit var tvGyroY: TextView
    private lateinit var tvGyroZ: TextView
    private lateinit var tvProx: TextView
    private lateinit var tvLux: TextView
    private lateinit var tvAzimuth: TextView
    private lateinit var tvMagAvail: TextView
    private lateinit var tvAccAvail: TextView
    private lateinit var tvGyroAvail: TextView
    private lateinit var tvProxAvail: TextView
    private lateinit var tvLightAvail: TextView
    private lateinit var tvAnomalyThreshold: TextView
    private lateinit var chart: LineChart

    private val chartEntries = mutableListOf<Entry>()
    private var chartIndex = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_sensors, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mag = MagnetometerManager(requireContext())
        bindViews(view)
        setupChart()
        populateAvailability()
        mag.startListening()

        val tick = object : Runnable {
            override fun run() {
                if (isAdded) refreshValues()
                handler.postDelayed(this, 300)
            }
        }
        handler.post(tick)

        view.findViewById<Button>(R.id.btn_calibrate_sensor).setOnClickListener {
            mag.calibrate()
            tvAnomalyThreshold.text =
                "Baseline: ${"%.1f".format(mag.baselineMagnetic)} µT  |  Threshold: ±${mag.anomalyThreshold.toInt()} µT"
        }
    }

    private fun bindViews(v: View) {
        tvMagX             = v.findViewById(R.id.tv_sensor_mag_x)
        tvMagY             = v.findViewById(R.id.tv_sensor_mag_y)
        tvMagZ             = v.findViewById(R.id.tv_sensor_mag_z)
        tvMagTotal         = v.findViewById(R.id.tv_sensor_mag_total)
        pbMagStrength      = v.findViewById(R.id.pb_mag_strength)
        tvAccX             = v.findViewById(R.id.tv_sensor_acc_x)
        tvAccY             = v.findViewById(R.id.tv_sensor_acc_y)
        tvAccZ             = v.findViewById(R.id.tv_sensor_acc_z)
        tvGyroX            = v.findViewById(R.id.tv_sensor_gyro_x)
        tvGyroY            = v.findViewById(R.id.tv_sensor_gyro_y)
        tvGyroZ            = v.findViewById(R.id.tv_sensor_gyro_z)
        tvProx             = v.findViewById(R.id.tv_sensor_prox)
        tvLux              = v.findViewById(R.id.tv_sensor_lux)
        tvAzimuth          = v.findViewById(R.id.tv_sensor_azimuth)
        tvMagAvail         = v.findViewById(R.id.tv_avail_mag)
        tvAccAvail         = v.findViewById(R.id.tv_avail_acc)
        tvGyroAvail        = v.findViewById(R.id.tv_avail_gyro)
        tvProxAvail        = v.findViewById(R.id.tv_avail_prox)
        tvLightAvail       = v.findViewById(R.id.tv_avail_light)
        tvAnomalyThreshold = v.findViewById(R.id.tv_anomaly_threshold)
        chart              = v.findViewById(R.id.chart_sensors)
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            legend.textColor = Color.parseColor("#00E5FF")
            legend.textSize = 11f

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#757575")
                setAvoidFirstLastClipping(true)
                granularity = 1f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#757575")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1E1E24")
            }
            axisRight.isEnabled = false
        }
    }

    private fun updateChart(value: Float) {
        chartEntries.add(Entry(chartIndex++, value))
        if (chartEntries.size > 80) chartEntries.removeAt(0)

        val dataSet = LineDataSet(chartEntries.toList(), "µT").apply {
            color = Color.parseColor("#00E5FF")
            setCircleColor(Color.parseColor("#00E5FF"))
            circleRadius = 1.5f
            lineWidth = 1.8f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#00E5FF")
            fillAlpha = 30
        }

        chart.data = LineData(dataSet)
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun populateAvailability() {
        fun avail(b: Boolean) = if (b) "✓ Present" else "✗ Not Found"
        tvMagAvail.text   = avail(mag.isMagnetometerAvailable)
        tvAccAvail.text   = avail(mag.isAccelerometerAvailable)
        tvGyroAvail.text  = avail(mag.isGyroscopeAvailable)
        tvProxAvail.text  = avail(mag.isProximityAvailable)
        tvLightAvail.text = avail(mag.isLightAvailable)
    }

    private fun refreshValues() {
        val mf = mag.magneticField
        tvMagX.text    = "X: ${"%.3f".format(mf[0])} µT"
        tvMagY.text    = "Y: ${"%.3f".format(mf[1])} µT"
        tvMagZ.text    = "Z: ${"%.3f".format(mf[2])} µT"
        val total = mag.magneticStrength
        tvMagTotal.text = "Magnitude: ${"%.2f".format(total)} µT"
        pbMagStrength.progress = min(150, total.toInt())
        updateChart(total)

        val a = mag.acceleration
        tvAccX.text = "X: ${"%.4f".format(a[0])} m/s²"
        tvAccY.text = "Y: ${"%.4f".format(a[1])} m/s²"
        tvAccZ.text = "Z: ${"%.4f".format(a[2])} m/s²"

        val g = mag.gyroscope
        tvGyroX.text = "X: ${"%.4f".format(g[0])} rad/s"
        tvGyroY.text = "Y: ${"%.4f".format(g[1])} rad/s"
        tvGyroZ.text = "Z: ${"%.4f".format(g[2])} rad/s"

        tvProx.text    = if (mag.proximity >= 0) "${"%.1f".format(mag.proximity)} cm" else "N/A"
        tvLux.text     = if (mag.lightLevel >= 0) "${"%.0f".format(mag.lightLevel)} lux" else "N/A"
        tvAzimuth.text = "Azimuth: ${"%.1f".format(mag.azimuth)}°"
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        mag.stopListening()
        super.onDestroyView()
    }
}
