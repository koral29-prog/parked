package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_sessions")
data class ParkingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val addressOrName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val floor: String? = null,
    val section: String? = null,
    val spotNumber: String? = null,
    val note: String? = null,
    val photoUri: String? = null,
    val meterExpiryTime: Long? = null,
    val isActive: Boolean = true
)
