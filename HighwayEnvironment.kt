package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class WeatherCondition(val label: String) {
    SUNNY("Clear Sun"),
    SNOW("Snowfall"),
    NIGHT("Midnight"),
    TUNNEL("Subterranean")
}

enum class HighwayMap(
    val id: String,
    val title: String,
    val subtitle: String,
    val weather: WeatherCondition,
    val cashMultiplier: Float,
    val skyTop: Color,
    val skyBottom: Color,
    val roadBase: Color,
    val roadStripe: Color,
    val shoulderColor: Color,
    val accentGlow: Color
) {
    DAYLIGHT_HIGHWAY(
        id = "daylight_highway",
        title = "Daylight Highway",
        subtitle = "Sunlit 4-lane coastal expressway",
        weather = WeatherCondition.SUNNY,
        cashMultiplier = 1.0f,
        skyTop = Color(0xFF1A6AC4),
        skyBottom = Color(0xFF8CC8F8),
        roadBase = Color(0xFF2C313B),
        roadStripe = Color(0xFF39404D),
        shoulderColor = Color(0xFF3A7D44),
        accentGlow = Color(0xFFFFC107)
    ),
    SNOWY_MOUNTAIN(
        id = "snowy_mountain",
        title = "Snowy Mountain",
        subtitle = "Icy alpine pass with falling snow",
        weather = WeatherCondition.SNOW,
        cashMultiplier = 1.25f,
        skyTop = Color(0xFF4C647E),
        skyBottom = Color(0xFFB8CCE0),
        roadBase = Color(0xFF384252),
        roadStripe = Color(0xFF475468),
        shoulderColor = Color(0xFFDCE7F2),
        accentGlow = Color(0xFF80DEEA)
    ),
    NIGHT_CITY(
        id = "night_city",
        title = "Night City",
        subtitle = "Neon skyline & streetlamp pools",
        weather = WeatherCondition.NIGHT,
        cashMultiplier = 1.4f,
        skyTop = Color(0xFF040712),
        skyBottom = Color(0xFF141E38),
        roadBase = Color(0xFF161A24),
        roadStripe = Color(0xFF1F2533),
        shoulderColor = Color(0xFF0D111A),
        accentGlow = Color(0xFFFF8F00)
    ),
    TUNNEL_ROAD(
        id = "tunnel_road",
        title = "Tunnel Road",
        subtitle = "High-speed lit metro tunnel",
        weather = WeatherCondition.TUNNEL,
        cashMultiplier = 1.5f,
        skyTop = Color(0xFF0B0E14),
        skyBottom = Color(0xFF1B2230),
        roadBase = Color(0xFF1C2029),
        roadStripe = Color(0xFF272D3B),
        shoulderColor = Color(0xFF282E3A),
        accentGlow = Color(0xFF00E5FF)
    );

    companion object {
        fun byId(id: String): HighwayMap = entries.firstOrNull { it.id == id } ?: DAYLIGHT_HIGHWAY
    }
}

enum class SteeringMode(val label: String) {
    TOUCH_BUTTONS("Touch Buttons"),
    GYROSCOPE_TILT("Gyroscope Tilt")
}

enum class TrafficMode(val label: String, val bonusMultiplier: Float) {
    ONE_WAY("One-Way", 1.0f),
    TWO_WAY("Two-Way (+50%)", 1.5f)
}
