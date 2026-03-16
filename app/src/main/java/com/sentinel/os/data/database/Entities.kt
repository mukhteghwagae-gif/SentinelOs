package com.sentinel.os.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val anomaliesDetected: Int = 0,
    val maxMagneticField: Float = 0f,
    val averageMagneticField: Float = 0f,
    val encryptedData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanSessionEntity

        if (id != other.id) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (anomaliesDetected != other.anomaliesDetected) return false
        if (maxMagneticField != other.maxMagneticField) return false
        if (averageMagneticField != other.averageMagneticField) return false
        if (encryptedData != null) {
            if (other.encryptedData == null) return false
            if (!encryptedData.contentEquals(other.encryptedData)) return false
        } else if (other.encryptedData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + (endTime?.hashCode() ?: 0)
        result = 31 * result + anomaliesDetected
        result = 31 * result + maxMagneticField.hashCode()
        result = 31 * result + averageMagneticField.hashCode()
        result = 31 * result + (encryptedData?.contentHashCode() ?: 0)
        return result
    }
}

@Entity(tableName = "magnetic_readings")
data class MagneticReadingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val isAnomaly: Boolean = false
)

@Entity(tableName = "sensor_events")
data class SensorEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sensorType: String,
    val eventType: String,
    val value: Float,
    val confidence: Float = 0f
)

@Entity(tableName = "threat_scores")
data class ThreatScoreEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Float,
    val threatLevel: String,
    val contributingFactors: String // JSON serialized
)

@Entity(tableName = "mesh_nodes")
data class MeshNodeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nodeId: String,
    val lastSeen: Long = System.currentTimeMillis(),
    val rssi: Int = 0,
    val nodeType: String,
    val isActive: Boolean = true
)

@Entity(tableName = "mesh_messages")
data class MeshMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val messageId: String,
    val sourceNodeId: String,
    val destinationNodeId: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: ByteArray,
    val isForwarded: Boolean = false,
    val encryptedData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MeshMessageEntity

        if (id != other.id) return false
        if (messageId != other.messageId) return false
        if (sourceNodeId != other.sourceNodeId) return false
        if (destinationNodeId != other.destinationNodeId) return false
        if (timestamp != other.timestamp) return false
        if (!payload.contentEquals(other.payload)) return false
        if (isForwarded != other.isForwarded) return false
        if (encryptedData != null) {
            if (other.encryptedData == null) return false
            if (!encryptedData.contentEquals(other.encryptedData)) return false
        } else if (other.encryptedData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + sourceNodeId.hashCode()
        result = 31 * result + (destinationNodeId?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + isForwarded.hashCode()
        result = 31 * result + (encryptedData?.contentHashCode() ?: 0)
        return result
    }
}

@Entity(tableName = "night_sessions")
data class NightSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val alertsTriggered: Int = 0,
    val maxThreatScore: Float = 0f,
    val averageThreatScore: Float = 0f,
    val recordedAudioPath: String? = null,
    val recordedVideoPath: String? = null
)
