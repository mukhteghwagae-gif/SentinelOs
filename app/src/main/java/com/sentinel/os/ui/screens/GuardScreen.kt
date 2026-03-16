package com.sentinel.os.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.sentinel.os.service.NightGuardService
import com.sentinel.os.domain.usecase.ThreatFusionEngine
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GuardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: GuardViewModel = viewModel(factory = GuardViewModelFactory(context))

    val isGuardActive by viewModel.isGuardActive.collectAsState()
    val threatAssessment by viewModel.threatAssessment.collectAsState()

    val threatScore = threatAssessment?.threatScore ?: 0f
    val alertsTriggered = threatAssessment?.contributingFactors?.size ?: 0

    LaunchedEffect(Unit) {
        // Optionally start the service automatically or based on a setting
        // viewModel.startGuardService(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Night Guard Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Threat Level Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Guard Status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isGuardActive) "ACTIVE" else "INACTIVE",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isGuardActive) Color(0xFF4CAF50) else Color(0xFFB0BEC5)
                    )
                }

                Text(
                    text = "Threat Score",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { threatScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        threatScore < 25 -> Color(0xFF4CAF50)
                        threatScore < 50 -> Color(0xFFFFC107)
                        threatScore < 75 -> Color(0xFFFF9800)
                        else -> Color(0xFFEF5350)
                    }
                )
                Text(
                    text = "%.1f / 100".format(threatScore),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alerts Triggered:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = alertsTriggered.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (alertsTriggered > 0) Color(0xFFEF5350) else Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Sensor Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Active Sensors",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SensorStatusRow("Microphone", isGuardActive)
                SensorStatusRow("Camera", isGuardActive)
                SensorStatusRow("Accelerometer", isGuardActive)
                SensorStatusRow("Magnetometer", isGuardActive)
                SensorStatusRow("Light Sensor", false) // Placeholder, as light sensor is not yet implemented
            }
        }

        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.toggleGuardService(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = if (isGuardActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isGuardActive) "Stop" else "Start",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isGuardActive) "Deactivate" else "Activate")
            }
        }

        // Recent Events
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "No events recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SensorStatusRow(name: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isActive) "ACTIVE" else "INACTIVE",
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFB0BEC5)
        )
    }
}
