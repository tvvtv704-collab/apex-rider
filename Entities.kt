package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val cash: Int = 1500,
    val selectedBikeId: String = "vespa_150",
    val selectedMapId: String = "daylight_highway",
    val steeringModeName: String = "TOUCH_BUTTONS",
    val trafficModeName: String = "ONE_WAY",
    val bestScore: Int = 0,
    val bestDistanceKm: Float = 0f,
    val totalOvertakes: Int = 0,
    val totalRides: Int = 0
)

@Entity(tableName = "bike_ownership")
data class BikeOwnershipEntity(
    @PrimaryKey val bikeId: String,
    val isUnlocked: Boolean = false,
    val speedLevel: Int = 1,
    val handlingLevel: Int = 1,
    val brakingLevel: Int = 1
)

@Entity(tableName = "ride_history")
data class RideHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mapId: String,
    val bikeName: String,
    val score: Int,
    val distanceKm: Float,
    val topSpeedKmh: Int,
    val overtakes: Int,
    val cashEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)
