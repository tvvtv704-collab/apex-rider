package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class TrafficVehicleType(
    val widthRatio: Float,
    val heightRatio: Float,
    val minSpeedKmh: Float,
    val maxSpeedKmh: Float
) {
    SEDAN(0.46f, 0.34f, 65f, 95f),
    SPORTS_CAR(0.47f, 0.30f, 85f, 120f),
    SUV(0.52f, 0.42f, 62f, 90f),
    VAN(0.54f, 0.48f, 58f, 82f),
    CITY_BUS(0.62f, 0.72f, 48f, 68f),
    SEMI_TRUCK(0.64f, 0.78f, 45f, 65f);

    companion object {
        val bodyColors = listOf(
            Color(0xFFE53935),
            Color(0xFF1E88E5),
            Color(0xFFCFD8DC),
            Color(0xFFFFB300),
            Color(0xFF43A047),
            Color(0xFF546E7A),
            Color(0xFF8E24AA),
            Color(0xFFFB8C00)
        )
    }
}

data class TrafficVehicle(
    val id: Long,
    val type: TrafficVehicleType,
    val laneIndex: Int, // 0, 1, 2, 3
    val laneX: Float, // -1.5f to +1.5f in lane units
    val zPos: Float, // 0.0f (camera) to 1.0f (horizon)
    val speedKmh: Float,
    val colorIndex: Int,
    val isOncoming: Boolean,
    val overtakenAlready: Boolean = false
)

data class Snowflake(
    val x: Float, // 0..1
    val y: Float, // 0..1
    val sizeDp: Float,
    val speed: Float,
    val drift: Float
)

data class OvertakePopup(
    val id: Long,
    val title: String,
    val cashBonus: Int,
    val comboCount: Int,
    val remainingSec: Float
)

data class RideResult(
    val map: HighwayMap,
    val bikeName: String,
    val distanceKm: Float,
    val topSpeedKmh: Int,
    val score: Int,
    val overtakes: Int,
    val highSpeedSeconds: Float,
    val cashEarned: Int,
    val crashReason: String
)

data class BikeUiModel(
    val spec: BikeSpec,
    val isUnlocked: Boolean,
    val isSelected: Boolean,
    val speedLevel: Int,
    val handlingLevel: Int,
    val brakingLevel: Int
) {
    val currentTopSpeedKmh: Int get() = spec.effectiveTopSpeedKmh(speedLevel)
    val currentHandling: Int get() = spec.effectiveHandling(handlingLevel)
    val currentBraking: Int get() = spec.effectiveBraking(brakingLevel)
}

data class PlayerState(
    val cash: Int = 1500,
    val selectedBikeId: String = "vespa_150",
    val selectedMap: HighwayMap = HighwayMap.DAYLIGHT_HIGHWAY,
    val steeringMode: SteeringMode = SteeringMode.TOUCH_BUTTONS,
    val trafficMode: TrafficMode = TrafficMode.ONE_WAY,
    val bestScore: Int = 0,
    val bestDistanceKm: Float = 0f,
    val totalOvertakes: Int = 0,
    val totalRides: Int = 0,
    val bikes: List<BikeUiModel> = emptyList()
) {
    val selectedBike: BikeUiModel?
        get() = bikes.firstOrNull { it.spec.id == selectedBikeId } ?: bikes.firstOrNull()
}
