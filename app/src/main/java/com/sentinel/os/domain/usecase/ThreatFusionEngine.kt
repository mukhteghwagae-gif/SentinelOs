package com.sentinel.os.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class ThreatAssessment(
    val threatScore: Float,
    val threatLevel: String,
    val contributingFactors: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Threat Fusion Engine combines signals from all sensor layers.
 * Produces unified threat score from 0 to 100 every 500ms.
 */
class ThreatFusionEngine {
    private val _threatAssessment = MutableStateFlow<ThreatAssessment?>(null)
    val threatAssessment: Flow<ThreatAssessment?> = _threatAssessment.asStateFlow()

    private var acousticScore = 0f
    private var opticalScore = 0f
    private var vibrationScore = 0f
    private var magneticScore = 0f
    private var rfScore = 0f

    private val weights = mapOf(
        "acoustic" to 0.25f,
        "optical" to 0.25f,
        "vibration" to 0.20f,
        "magnetic" to 0.15f,
        "rf" to 0.15f
    )

    fun updateAcousticScore(score: Float) {
        acousticScore = score.coerceIn(0f, 100f)
        computeThreatScore()
    }

    fun updateOpticalScore(score: Float) {
        opticalScore = score.coerceIn(0f, 100f)
        computeThreatScore()
    }

    fun updateVibrationScore(score: Float) {
        vibrationScore = score.coerceIn(0f, 100f)
        computeThreatScore()
    }

    fun updateMagneticScore(score: Float) {
        magneticScore = score.coerceIn(0f, 100f)
        computeThreatScore()
    }

    fun updateRFScore(score: Float) {
        rfScore = score.coerceIn(0f, 100f)
        computeThreatScore()
    }

    private fun computeThreatScore() {
        val baseScore = (
            acousticScore * weights["acoustic"]!! +
            opticalScore * weights["optical"]!! +
            vibrationScore * weights["vibration"]!! +
            magneticScore * weights["magnetic"]!! +
            rfScore * weights["rf"]!!
        )

        // Apply correlation multipliers when multiple sensors confirm
        var correlationMultiplier = 1.0f
        val activeScores = listOf(acousticScore, opticalScore, vibrationScore, magneticScore, rfScore)
            .count { it > 30f }

        if (activeScores >= 2) {
            correlationMultiplier = 1.2f
        }
        if (activeScores >= 3) {
            correlationMultiplier = 1.5f
        }

        val finalScore = (baseScore * correlationMultiplier).coerceIn(0f, 100f)
        val threatLevel = getThreatLevel(finalScore)
        val factors = getContributingFactors()

        val assessment = ThreatAssessment(
            threatScore = finalScore,
            threatLevel = threatLevel,
            contributingFactors = factors
        )

        _threatAssessment.value = assessment
        Timber.d("Threat Score: $finalScore ($threatLevel)")
    }

    private fun getThreatLevel(score: Float): String {
        return when {
            score < 25 -> "LOW"
            score < 50 -> "MEDIUM"
            score < 75 -> "HIGH"
            else -> "CRITICAL"
        }
    }

    private fun getContributingFactors(): List<String> {
        val factors = mutableListOf<String>()

        if (acousticScore > 30) factors.add("Unusual sounds detected")
        if (opticalScore > 30) factors.add("Motion or brightness changes")
        if (vibrationScore > 30) factors.add("Vibration/impact detected")
        if (magneticScore > 30) factors.add("Magnetic field anomaly")
        if (rfScore > 30) factors.add("Unknown RF devices nearby")

        return factors.ifEmpty { listOf("No significant threats") }
    }

    fun reset() {
        acousticScore = 0f
        opticalScore = 0f
        vibrationScore = 0f
        magneticScore = 0f
        rfScore = 0f
        computeThreatScore()
    }
}
