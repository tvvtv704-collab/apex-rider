package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class BikeCategory(val displayName: String) {
    SCOOTER("Classic Scooter"),
    CRUISER("Heavy Cruiser"),
    SPORT("Sports & Naked"),
    SUPERBIKE("Superbike")
}

enum class HandlebarStyle {
    SCOOTER_COWL,
    CRUISER_APE,
    NAKED_UPRIGHT,
    SPORT_CLIPON
}

data class BikeSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: BikeCategory,
    val price: Int,
    val baseTopSpeedKmh: Int,
    val maxTopSpeedKmh: Int,
    val baseHandling: Int, // 1..100 scale
    val maxHandling: Int,
    val baseBraking: Int, // 1..100 scale
    val maxBraking: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val handlebarStyle: HandlebarStyle,
    val engineSpec: String
) {
    companion object {
        const val MAX_UPGRADE_LEVEL = 5
    }

    fun effectiveTopSpeedKmh(speedLevel: Int): Int {
        val lvl = speedLevel.coerceIn(1, MAX_UPGRADE_LEVEL)
        return baseTopSpeedKmh + ((maxTopSpeedKmh - baseTopSpeedKmh) * (lvl - 1) / (MAX_UPGRADE_LEVEL - 1))
    }

    fun effectiveHandling(handlingLevel: Int): Int {
        val lvl = handlingLevel.coerceIn(1, MAX_UPGRADE_LEVEL)
        return baseHandling + ((maxHandling - baseHandling) * (lvl - 1) / (MAX_UPGRADE_LEVEL - 1))
    }

    fun effectiveBraking(brakingLevel: Int): Int {
        val lvl = brakingLevel.coerceIn(1, MAX_UPGRADE_LEVEL)
        return baseBraking + ((maxBraking - baseBraking) * (lvl - 1) / (MAX_UPGRADE_LEVEL - 1))
    }

    fun upgradeCost(currentLevel: Int): Int {
        val base = (price.coerceAtLeast(800) * 0.16f).toInt()
        return ((base * currentLevel) / 50) * 50 + 250
    }
}

object BikeCatalog {
    val allBikes: List<BikeSpec> = listOf(
        // 1-4: Classic Scooters
        BikeSpec(
            id = "vespa_150",
            name = "Vespa Corsa 150",
            subtitle = "Agile Retro Commuter",
            category = BikeCategory.SCOOTER,
            price = 0,
            baseTopSpeedKmh = 128,
            maxTopSpeedKmh = 152,
            baseHandling = 48,
            maxHandling = 68,
            baseBraking = 44,
            maxBraking = 64,
            primaryColor = Color(0xFF26A69A),
            accentColor = Color(0xFFFFCA28),
            handlebarStyle = HandlebarStyle.SCOOTER_COWL,
            engineSpec = "150cc Single SOHC"
        ),
        BikeSpec(
            id = "urban_250",
            name = "Urban Glide 250",
            subtitle = "City Maxi Scooter",
            category = BikeCategory.SCOOTER,
            price = 1800,
            baseTopSpeedKmh = 142,
            maxTopSpeedKmh = 168,
            baseHandling = 52,
            maxHandling = 72,
            baseBraking = 48,
            maxBraking = 68,
            primaryColor = Color(0xFF42A5F5),
            accentColor = Color(0xFFE0F7FA),
            handlebarStyle = HandlebarStyle.SCOOTER_COWL,
            engineSpec = "249cc Liquid-Cooled"
        ),
        BikeSpec(
            id = "metro_300",
            name = "Metro Max 300",
            subtitle = "Twin-Lamp Touring Scoot",
            category = BikeCategory.SCOOTER,
            price = 3200,
            baseTopSpeedKmh = 155,
            maxTopSpeedKmh = 180,
            baseHandling = 55,
            maxHandling = 75,
            baseBraking = 52,
            maxBraking = 72,
            primaryColor = Color(0xFFEF5350),
            accentColor = Color(0xFFFFD54F),
            handlebarStyle = HandlebarStyle.SCOOTER_COWL,
            engineSpec = "298cc CVT Sport"
        ),
        BikeSpec(
            id = "tokyo_400",
            name = "Tokyo Runner 400",
            subtitle = "Expressway GT Scooter",
            category = BikeCategory.SCOOTER,
            price = 4800,
            baseTopSpeedKmh = 166,
            maxTopSpeedKmh = 192,
            baseHandling = 58,
            maxHandling = 78,
            baseBraking = 55,
            maxBraking = 75,
            primaryColor = Color(0xFFAB47BC),
            accentColor = Color(0xFF00E5FF),
            handlebarStyle = HandlebarStyle.SCOOTER_COWL,
            engineSpec = "399cc Twin-Cam"
        ),

        // 5-9: Heavy Cruisers
        BikeSpec(
            id = "iron_750",
            name = "Iron V-Twin 750",
            subtitle = "Classic Steel Bobber",
            category = BikeCategory.CRUISER,
            price = 6500,
            baseTopSpeedKmh = 175,
            maxTopSpeedKmh = 204,
            baseHandling = 50,
            maxHandling = 70,
            baseBraking = 54,
            maxBraking = 74,
            primaryColor = Color(0xFF8D6E63),
            accentColor = Color(0xFFFF8F00),
            handlebarStyle = HandlebarStyle.CRUISER_APE,
            engineSpec = "749cc V-Twin"
        ),
        BikeSpec(
            id = "thunder_950",
            name = "Thunder Bolt 950",
            subtitle = "Dark Custom Cruiser",
            category = BikeCategory.CRUISER,
            price = 9200,
            baseTopSpeedKmh = 188,
            maxTopSpeedKmh = 218,
            baseHandling = 54,
            maxHandling = 74,
            baseBraking = 58,
            maxBraking = 78,
            primaryColor = Color(0xFF37474F),
            accentColor = Color(0xFFFF5722),
            handlebarStyle = HandlebarStyle.CRUISER_APE,
            engineSpec = "942cc Air-Cooled V2"
        ),
        BikeSpec(
            id = "outlaw_1200",
            name = "Outlaw Chopper 1200",
            subtitle = "Chrome Highway Muscle",
            category = BikeCategory.CRUISER,
            price = 12800,
            baseTopSpeedKmh = 202,
            maxTopSpeedKmh = 232,
            baseHandling = 56,
            maxHandling = 76,
            baseBraking = 60,
            maxBraking = 80,
            primaryColor = Color(0xFFD32F2F),
            accentColor = Color(0xFFE0E0E0),
            handlebarStyle = HandlebarStyle.CRUISER_APE,
            engineSpec = "1202cc Evolution V-Twin"
        ),
        BikeSpec(
            id = "roadking_1600",
            name = "Road King 1600",
            subtitle = "Bagger Mile Crusher",
            category = BikeCategory.CRUISER,
            price = 16500,
            baseTopSpeedKmh = 212,
            maxTopSpeedKmh = 244,
            baseHandling = 58,
            maxHandling = 79,
            baseBraking = 63,
            maxBraking = 83,
            primaryColor = Color(0xFF1E88E5),
            accentColor = Color(0xFFFFB300),
            handlebarStyle = HandlebarStyle.CRUISER_APE,
            engineSpec = "1584cc Twin-Cam"
        ),
        BikeSpec(
            id = "valkyrie_1800",
            name = "Valkyrie Titan 1800",
            subtitle = "Flat-Six Power Cruiser",
            category = BikeCategory.CRUISER,
            price = 21000,
            baseTopSpeedKmh = 224,
            maxTopSpeedKmh = 258,
            baseHandling = 62,
            maxHandling = 82,
            baseBraking = 66,
            maxBraking = 86,
            primaryColor = Color(0xFF5C6BC0),
            accentColor = Color(0xFF00E5FF),
            handlebarStyle = HandlebarStyle.CRUISER_APE,
            engineSpec = "1832cc Flat-6"
        ),

        // 10-16: Sports & Naked
        BikeSpec(
            id = "hornet_300",
            name = "Street Hornet 300",
            subtitle = "Featherweight Naked",
            category = BikeCategory.SPORT,
            price = 4200,
            baseTopSpeedKmh = 168,
            maxTopSpeedKmh = 196,
            baseHandling = 64,
            maxHandling = 84,
            baseBraking = 60,
            maxBraking = 80,
            primaryColor = Color(0xFFFFA726),
            accentColor = Color(0xFFECEFF1),
            handlebarStyle = HandlebarStyle.NAKED_UPRIGHT,
            engineSpec = "296cc Parallel Twin"
        ),
        BikeSpec(
            id = "apex_600",
            name = "Apex Naked 600",
            subtitle = "Razor Streetfighter",
            category = BikeCategory.SPORT,
            price = 8800,
            baseTopSpeedKmh = 205,
            maxTopSpeedKmh = 236,
            baseHandling = 70,
            maxHandling = 88,
            baseBraking = 66,
            maxBraking = 86,
            primaryColor = Color(0xFF29B6F6),
            accentColor = Color(0xFFFF8F00),
            handlebarStyle = HandlebarStyle.NAKED_UPRIGHT,
            engineSpec = "599cc Inline-4"
        ),
        BikeSpec(
            id = "viper_650",
            name = "Viper RR 650",
            subtitle = "Middleweight Track Twin",
            category = BikeCategory.SPORT,
            price = 11500,
            baseTopSpeedKmh = 218,
            maxTopSpeedKmh = 250,
            baseHandling = 73,
            maxHandling = 90,
            baseBraking = 70,
            maxBraking = 88,
            primaryColor = Color(0xFF66BB6A),
            accentColor = Color(0xFF1B5E20),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "649cc DOHC Twin"
        ),
        BikeSpec(
            id = "kuro_750",
            name = "Kuro Shadow 750",
            subtitle = "Midnight Street Assassin",
            category = BikeCategory.SPORT,
            price = 15200,
            baseTopSpeedKmh = 232,
            maxTopSpeedKmh = 264,
            baseHandling = 76,
            maxHandling = 92,
            baseBraking = 72,
            maxBraking = 90,
            primaryColor = Color(0xFF455A64),
            accentColor = Color(0xFF00E5FF),
            handlebarStyle = HandlebarStyle.NAKED_UPRIGHT,
            engineSpec = "749cc Triple"
        ),
        BikeSpec(
            id = "tornado_900",
            name = "Tornado 900",
            subtitle = "Torque-Monster Naked",
            category = BikeCategory.SPORT,
            price = 19500,
            baseTopSpeedKmh = 244,
            maxTopSpeedKmh = 276,
            baseHandling = 78,
            maxHandling = 93,
            baseBraking = 75,
            maxBraking = 92,
            primaryColor = Color(0xFFFF7043),
            accentColor = Color(0xFFFFEB3B),
            handlebarStyle = HandlebarStyle.NAKED_UPRIGHT,
            engineSpec = "890cc Crossplane-3"
        ),
        BikeSpec(
            id = "corsa_998",
            name = "Corsa 998 Track",
            subtitle = "Italian V-Twin Legend",
            category = BikeCategory.SPORT,
            price = 24500,
            baseTopSpeedKmh = 256,
            maxTopSpeedKmh = 288,
            baseHandling = 80,
            maxHandling = 95,
            baseBraking = 78,
            maxBraking = 94,
            primaryColor = Color(0xFFE53935),
            accentColor = Color(0xFFFFFFFF),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "998cc Desmo V-Twin"
        ),
        BikeSpec(
            id = "phantom_r1000",
            name = "Phantom R1000",
            subtitle = "Endurance Spec Fighter",
            category = BikeCategory.SPORT,
            price = 28500,
            baseTopSpeedKmh = 268,
            maxTopSpeedKmh = 300,
            baseHandling = 82,
            maxHandling = 96,
            baseBraking = 80,
            maxBraking = 95,
            primaryColor = Color(0xFF5E35B1),
            accentColor = Color(0xFF00E676),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "999cc Big-Bang I4"
        ),

        // 17-22: Superbikes
        BikeSpec(
            id = "ninja_zxr",
            name = "Kawa ZX-R 1000",
            subtitle = "Track-Bred Apex Hunter",
            category = BikeCategory.SUPERBIKE,
            price = 33000,
            baseTopSpeedKmh = 282,
            maxTopSpeedKmh = 316,
            baseHandling = 84,
            maxHandling = 97,
            baseBraking = 83,
            maxBraking = 96,
            primaryColor = Color(0xFF00E676),
            accentColor = Color(0xFF212121),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "998cc Ram-Air Inline-4"
        ),
        BikeSpec(
            id = "hayabusa_1300",
            name = "Falcon Blitz 1300",
            subtitle = "Aerodynamic Speed King",
            category = BikeCategory.SUPERBIKE,
            price = 38500,
            baseTopSpeedKmh = 296,
            maxTopSpeedKmh = 332,
            baseHandling = 80,
            maxHandling = 94,
            baseBraking = 84,
            maxBraking = 96,
            primaryColor = Color(0xFF2979FF),
            accentColor = Color(0xFFFFAB00),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "1340cc Hyper-4"
        ),
        BikeSpec(
            id = "panigale_v4r",
            name = "Veloce V4R Carbon",
            subtitle = "Winglet Superbike",
            category = BikeCategory.SUPERBIKE,
            price = 46000,
            baseTopSpeedKmh = 308,
            maxTopSpeedKmh = 344,
            baseHandling = 87,
            maxHandling = 98,
            baseBraking = 87,
            maxBraking = 98,
            primaryColor = Color(0xFFFF1744),
            accentColor = Color(0xFFFFC400),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "998cc Stradale V4"
        ),
        BikeSpec(
            id = "s1000rr_carbon",
            name = "Bavaria RR Carbon",
            subtitle = "ShiftCam Precision",
            category = BikeCategory.SUPERBIKE,
            price = 54000,
            baseTopSpeedKmh = 318,
            maxTopSpeedKmh = 354,
            baseHandling = 89,
            maxHandling = 99,
            baseBraking = 89,
            maxBraking = 99,
            primaryColor = Color(0xFFECEFF1),
            accentColor = Color(0xFF2962FF),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "999cc ShiftCam I4"
        ),
        BikeSpec(
            id = "h2r_hyper",
            name = "Vortex H2R Charged",
            subtitle = "Supercharged Missile",
            category = BikeCategory.SUPERBIKE,
            price = 65000,
            baseTopSpeedKmh = 334,
            maxTopSpeedKmh = 372,
            baseHandling = 88,
            maxHandling = 99,
            baseBraking = 90,
            maxBraking = 99,
            primaryColor = Color(0xFF263238),
            accentColor = Color(0xFF00E676),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "998cc Supercharged I4"
        ),
        BikeSpec(
            id = "apex_gp_proto",
            name = "Apex MotoGP Proto",
            subtitle = "Factory Carbon Prototype",
            category = BikeCategory.SUPERBIKE,
            price = 82000,
            baseTopSpeedKmh = 348,
            maxTopSpeedKmh = 390,
            baseHandling = 92,
            maxHandling = 100,
            baseBraking = 92,
            maxBraking = 100,
            primaryColor = Color(0xFFFF8F00),
            accentColor = Color(0xFF00E5FF),
            handlebarStyle = HandlebarStyle.SPORT_CLIPON,
            engineSpec = "1000cc Factory V4"
        )
    )

    fun byId(id: String): BikeSpec = allBikes.firstOrNull { it.id == id } ?: allBikes.first()
}
