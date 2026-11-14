package com.example.game

import com.example.domain.model.BikeUiModel
import com.example.domain.model.HighwayMap
import com.example.domain.model.OvertakePopup
import com.example.domain.model.RideResult
import com.example.domain.model.Snowflake
import com.example.domain.model.TrafficMode
import com.example.domain.model.TrafficVehicle
import com.example.domain.model.TrafficVehicleType
import com.example.domain.model.WeatherCondition
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class RaceEngine(
    val bike: BikeUiModel,
    val map: HighwayMap,
    val trafficMode: TrafficMode
) {
    companion object {
        val LANE_CENTERS = floatArrayOf(-1.35f, -0.45f, 0.45f, 1.35f)
        const val ROAD_LIMIT_X = 1.68f
    }

    var speedKmh: Float = 45f
        private set
    var topSpeedReachedKmh: Int = 45
        private set
    var playerX: Float = 0.45f
        private set
    var leanAngleDeg: Float = 0f
        private set
    var suspensionBob: Float = 0f
        private set
    var roadScroll: Float = 0f
        private set
    var distanceMeters: Float = 0f
        private set
    var score: Float = 0f
        private set
    var sessionCash: Float = 0f
        private set
    var overtakes: Int = 0
        private set
    var highSpeedSeconds: Float = 0f
        private set
    var comboCount: Int = 0
        private set
    var comboTimerSec: Float = 0f
        private set
    var gear: Int = 2
        private set
    var rpmRatio: Float = 0.35f
        private set
    var isCrashed: Boolean = false
        private set
    var crashShake: Float = 0f
        private set
    var crashReason: String = ""
        private set
    var isBrakingVisual: Boolean = false
        private set
    var isThrottleVisual: Boolean = false
        private set

    val vehicles = mutableListOf<TrafficVehicle>()
    val popups = mutableListOf<OvertakePopup>()
    val snowflakes = mutableListOf<Snowflake>()

    private var nextVehicleId = 1L
    private var nextPopupId = 1L
    private var spawnCooldownSec = 0.4f
    private var elapsedSec = 0f
    private var highSpeedCashAccumulator = 0f

    private val maxSpeed = bike.currentTopSpeedKmh.toFloat()
    private val handlingStat = bike.currentHandling
    private val brakingStat = bike.currentBraking

    init {
        if (map.weather == WeatherCondition.SNOW) {
            repeat(65) {
                snowflakes += Snowflake(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    sizeDp = Random.nextFloat() * 3.5f + 1.8f,
                    speed = Random.nextFloat() * 0.28f + 0.16f,
                    drift = (Random.nextFloat() - 0.5f) * 0.12f
                )
            }
        }
        // Seed 3 distant vehicles ahead on start
        spawnTrafficVehicle(initialZ = 0.48f)
        spawnTrafficVehicle(initialZ = 0.72f)
        spawnTrafficVehicle(initialZ = 0.94f)
    }

    fun step(
        dtSec: Float,
        throttlePressed: Boolean,
        brakePressed: Boolean,
        steerInput: Float
    ): Boolean {
        val dt = dtSec.coerceIn(0.001f, 0.05f)
        elapsedSec += dt

        if (isCrashed) {
            crashShake = (crashShake - dt * 2.5f).coerceAtLeast(0f)
            updatePopups(dt)
            updateSnow(dt)
            return false
        }

        isThrottleVisual = throttlePressed
        isBrakingVisual = brakePressed

        // 1. Speed & Gear Physics
        val accelFactor = 28f + (maxSpeed / 10f)
        val brakeFactor = 65f + brakingStat * 1.45f
        val dragFactor = 10f + (speedKmh / maxSpeed) * 18f

        when {
            brakePressed -> speedKmh -= brakeFactor * dt
            throttlePressed -> {
                val powerCurve = (1.12f - (speedKmh / maxSpeed) * 0.75f).coerceAtLeast(0.18f)
                speedKmh += accelFactor * powerCurve * dt
            }
            else -> speedKmh -= dragFactor * dt
        }
        speedKmh = speedKmh.coerceIn(28f, maxSpeed)
        if (speedKmh.toInt() > topSpeedReachedKmh) {
            topSpeedReachedKmh = speedKmh.toInt()
        }

        val speedRatio = (speedKmh / maxSpeed).coerceIn(0f, 1f)
        gear = (1 + (speedRatio * 5.2f).toInt()).coerceIn(1, 6)
        val gearSpan = 1f / 6f
        val inGearPos = ((speedRatio - (gear - 1) * gearSpan) / gearSpan).coerceIn(0.18f, 0.98f)
        rpmRatio = if (throttlePressed) (inGearPos + 0.08f).coerceAtMost(1f) else (inGearPos * 0.85f)

        // 2. Lateral Steering & Bike Lean
        val steerResponsiveness = 1.65f + (handlingStat / 100f) * 1.65f
        val clampedSteer = steerInput.coerceIn(-1f, 1f)
        playerX += clampedSteer * steerResponsiveness * dt
        playerX = playerX.coerceIn(-ROAD_LIMIT_X, ROAD_LIMIT_X)

        val targetLean = clampedSteer * 24f
        leanAngleDeg += (targetLean - leanAngleDeg) * (dt * 12f).coerceAtMost(1f)
        suspensionBob = sin(elapsedSec * (6f + speedKmh * 0.08f)) * (0.4f + speedRatio * 1.4f)

        // 3. Road Scroll & Distance
        val metersPerSec = speedKmh / 3.6f
        distanceMeters += metersPerSec * dt
        roadScroll = (roadScroll + metersPerSec * dt * 0.085f) % 1f

        // 4. Score & High-Speed >100 km/h Bonus
        val highSpeedActive = speedKmh >= 100f
        val speedMultiplier = when {
            speedKmh >= 220f -> 3.0f
            speedKmh >= 160f -> 2.3f
            highSpeedActive -> 1.7f
            else -> 1.0f
        }
        score += metersPerSec * dt * 2.2f * speedMultiplier * trafficMode.bonusMultiplier * map.cashMultiplier
        if (highSpeedActive) {
            highSpeedSeconds += dt
            highSpeedCashAccumulator += dt * 10f * map.cashMultiplier * trafficMode.bonusMultiplier
            if (highSpeedCashAccumulator >= 1f) {
                val whole = highSpeedCashAccumulator.toInt()
                sessionCash += whole
                highSpeedCashAccumulator -= whole
            }
        }
        sessionCash += metersPerSec * dt * 0.16f * map.cashMultiplier

        // Combo decay
        if (comboTimerSec > 0f) {
            comboTimerSec -= dt
            if (comboTimerSec <= 0f) comboCount = 0
        }

        // 5. Traffic Update, Overtaking & Collision
        spawnCooldownSec -= dt
        if (spawnCooldownSec <= 0f && vehicles.size < 11) {
            spawnTrafficVehicle(initialZ = 1.0f)
            val densityDelay = (0.92f - (speedKmh / 420f)).coerceIn(0.32f, 0.85f)
            spawnCooldownSec = densityDelay + Random.nextFloat() * 0.35f
        }

        val iterator = vehicles.listIterator()
        while (iterator.hasNext()) {
            val v = iterator.next()
            // Relative Z speed: positive means approaching camera (z decreasing toward 0)
            val relativeKmh = if (v.isOncoming) {
                speedKmh + v.speedKmh
            } else {
                speedKmh - v.speedKmh
            }
            val zDelta = (relativeKmh / 3.6f) * dt * 0.0082f
            val newZ = v.zPos - zDelta

            // Collision & Close Overtake check when passing rider plane
            val collisionHalfWidth = v.type.widthRatio * 0.56f + 0.14f
            val overtakeRadius = collisionHalfWidth + 0.42f
            val dx = abs(playerX - v.laneX)

            if (newZ in 0.025f..0.092f && dx < collisionHalfWidth) {
                isCrashed = true
                crashShake = 1.0f
                crashReason = when {
                    v.isOncoming -> "Head-on collision in oncoming lane"
                    v.type == TrafficVehicleType.SEMI_TRUCK -> "Clipped a heavy freight semi-truck"
                    v.type == TrafficVehicleType.CITY_BUS -> "Rear-ended a city transit bus"
                    else -> "High-speed traffic impact"
                }
                return true
            }

            var updatedOvertake = v.overtakenAlready
            if (!updatedOvertake && newZ < 0.055f && newZ > -0.04f && speedKmh >= 80f) {
                if (dx <= overtakeRadius) {
                    updatedOvertake = true
                    overtakes += 1
                    comboCount = (comboCount + 1).coerceAtMost(5)
                    comboTimerSec = 3.2f
                    val bonusCash = ((40 + (speedKmh * 0.18f).toInt()) * comboCount *
                        trafficMode.bonusMultiplier * map.cashMultiplier).toInt()
                    sessionCash += bonusCash
                    score += 150f * comboCount
                    val title = if (v.isOncoming) "ONCOMING NEAR MISS!" else "CLOSE OVERTAKE!"
                    popups += OvertakePopup(
                        id = nextPopupId++,
                        title = title,
                        cashBonus = bonusCash,
                        comboCount = comboCount,
                        remainingSec = 1.4f
                    )
                }
            }

            if (newZ < -0.14f || newZ > 1.25f) {
                iterator.remove()
            } else {
                iterator.set(v.copy(zPos = newZ, overtakenAlready = updatedOvertake))
            }
        }

        updatePopups(dt)
        updateSnow(dt)
        return false
    }

    private fun updatePopups(dt: Float) {
        val it = popups.listIterator()
        while (it.hasNext()) {
            val p = it.next()
            val rem = p.remainingSec - dt
            if (rem <= 0f) it.remove() else it.set(p.copy(remainingSec = rem))
        }
    }

    private fun updateSnow(dt: Float) {
        if (map.weather != WeatherCondition.SNOW) return
        for (i in snowflakes.indices) {
            val s = snowflakes[i]
            var nx = s.x + s.drift * dt - (leanAngleDeg * 0.0025f * dt)
            var ny = s.y + s.speed * dt * (1f + speedKmh / 240f)
            if (ny > 1f) {
                ny = 0f
                nx = Random.nextFloat()
            }
            if (nx < 0f) nx += 1f
            if (nx > 1f) nx -= 1f
            snowflakes[i] = s.copy(x = nx, y = ny)
        }
    }

    private fun spawnTrafficVehicle(initialZ: Float) {
        // Never block all 4 lanes near the same Z band
        val nearby = vehicles.filter { abs(it.zPos - initialZ) < 0.18f }.map { it.laneIndex }.toSet()
        val availableLanes = (0..3).filter { it !in nearby }
        if (availableLanes.isEmpty() || nearby.size >= 3) return

        val laneIdx = availableLanes.random()
        val isOncoming = trafficMode == TrafficMode.TWO_WAY && laneIdx <= 1
        val types = TrafficVehicleType.entries
        val type = types[Random.nextInt(types.size)]
        val vSpeed = Random.nextFloat() * (type.maxSpeedKmh - type.minSpeedKmh) + type.minSpeedKmh

        vehicles += TrafficVehicle(
            id = nextVehicleId++,
            type = type,
            laneIndex = laneIdx,
            laneX = LANE_CENTERS[laneIdx],
            zPos = initialZ,
            speedKmh = vSpeed,
            colorIndex = Random.nextInt(TrafficVehicleType.bodyColors.size),
            isOncoming = isOncoming
        )
    }

    fun buildResult(): RideResult = RideResult(
        map = map,
        bikeName = bike.spec.name,
        distanceKm = distanceMeters / 1000f,
        topSpeedKmh = topSpeedReachedKmh,
        score = score.toInt(),
        overtakes = overtakes,
        highSpeedSeconds = highSpeedSeconds,
        cashEarned = sessionCash.toInt().coerceAtLeast(15),
        crashReason = crashReason.ifBlank { "Ride ended" }
    )
}
