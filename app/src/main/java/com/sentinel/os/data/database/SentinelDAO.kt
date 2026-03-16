package com.sentinel.os.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {
    @Insert
    suspend fun insert(session: ScanSessionEntity): Long

    @Update
    suspend fun update(session: ScanSessionEntity)

    @Delete
    suspend fun delete(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<ScanSessionEntity?>

    @Query("SELECT * FROM scan_sessions WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getSessionsAfter(startTime: Long): Flow<List<ScanSessionEntity>>
}

@Dao
interface MagneticReadingDao {
    @Insert
    suspend fun insert(reading: MagneticReadingEntity): Long

    @Insert
    suspend fun insertAll(readings: List<MagneticReadingEntity>)

    @Query("SELECT * FROM magnetic_readings WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getReadingsBySession(sessionId: String): Flow<List<MagneticReadingEntity>>

    @Query("SELECT * FROM magnetic_readings WHERE isAnomaly = 1 ORDER BY timestamp DESC")
    fun getAnomalousReadings(): Flow<List<MagneticReadingEntity>>

    @Query("DELETE FROM magnetic_readings WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}

@Dao
interface SensorEventDao {
    @Insert
    suspend fun insert(event: SensorEventEntity): Long

    @Insert
    suspend fun insertAll(events: List<SensorEventEntity>)

    @Query("SELECT * FROM sensor_events WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getEventsBySession(sessionId: String): Flow<List<SensorEventEntity>>

    @Query("SELECT * FROM sensor_events WHERE sensorType = :sensorType ORDER BY timestamp DESC LIMIT :limit")
    fun getEventsBySensorType(sensorType: String, limit: Int = 100): Flow<List<SensorEventEntity>>
}

@Dao
interface ThreatScoreDao {
    @Insert
    suspend fun insert(score: ThreatScoreEntity): Long

    @Query("SELECT * FROM threat_scores WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getScoresBySession(sessionId: String): Flow<List<ThreatScoreEntity>>

    @Query("SELECT * FROM threat_scores ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScores(limit: Int = 100): Flow<List<ThreatScoreEntity>>

    @Query("SELECT MAX(score) FROM threat_scores WHERE sessionId = :sessionId")
    fun getMaxScoreForSession(sessionId: String): Flow<Float?>
}

@Dao
interface MeshNodeDao {
    @Insert
    suspend fun insert(node: MeshNodeEntity): Long

    @Update
    suspend fun update(node: MeshNodeEntity)

    @Query("SELECT * FROM mesh_nodes WHERE isActive = 1 ORDER BY lastSeen DESC")
    fun getActiveNodes(): Flow<List<MeshNodeEntity>>

    @Query("SELECT * FROM mesh_nodes WHERE nodeId = :nodeId")
    fun getNodeById(nodeId: String): Flow<MeshNodeEntity?>

    @Query("UPDATE mesh_nodes SET isActive = 0 WHERE lastSeen < :threshold")
    suspend fun deactivateStaleNodes(threshold: Long)
}

@Dao
interface MeshMessageDao {
    @Insert
    suspend fun insert(message: MeshMessageEntity): Long

    @Query("SELECT * FROM mesh_messages WHERE sourceNodeId = :nodeId ORDER BY timestamp DESC")
    fun getMessagesByNode(nodeId: String): Flow<List<MeshMessageEntity>>

    @Query("SELECT * FROM mesh_messages WHERE isForwarded = 0 ORDER BY timestamp ASC")
    fun getPendingForwardMessages(): Flow<List<MeshMessageEntity>>

    @Update
    suspend fun update(message: MeshMessageEntity)

    @Query("DELETE FROM mesh_messages WHERE timestamp < :threshold")
    suspend fun deleteOldMessages(threshold: Long)
}

@Dao
interface NightSessionDao {
    @Insert
    suspend fun insert(session: NightSessionEntity): Long

    @Update
    suspend fun update(session: NightSessionEntity)

    @Query("SELECT * FROM night_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<NightSessionEntity>>

    @Query("SELECT * FROM night_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<NightSessionEntity?>

    @Query("SELECT * FROM night_sessions WHERE alertsTriggered > 0 ORDER BY startTime DESC")
    fun getSessionsWithAlerts(): Flow<List<NightSessionEntity>>
}
