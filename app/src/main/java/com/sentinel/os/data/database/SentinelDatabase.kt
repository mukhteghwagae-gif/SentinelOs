package com.sentinel.os.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanSessionEntity::class,
        MagneticReadingEntity::class,
        SensorEventEntity::class,
        ThreatScoreEntity::class,
        MeshNodeEntity::class,
        MeshMessageEntity::class,
        NightSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun magneticReadingDao(): MagneticReadingDao
    abstract fun sensorEventDao(): SensorEventDao
    abstract fun threatScoreDao(): ThreatScoreDao
    abstract fun meshNodeDao(): MeshNodeDao
    abstract fun meshMessageDao(): MeshMessageDao
    abstract fun nightSessionDao(): NightSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SentinelDatabase? = null

        fun getInstance(context: Context): SentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentinelDatabase::class.java,
                    "sentinel_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
