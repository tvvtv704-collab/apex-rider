package com.example

import com.example.domain.model.BikeCatalog
import com.example.domain.model.BikeUiModel
import com.example.domain.model.HighwayMap
import com.example.domain.model.TrafficMode
import com.example.game.RaceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceEngineUnitTest {

    @Test
    fun bikeCatalogHasAtLeast22UniqueBikes() {
        val bikes = BikeCatalog.allBikes
        assertEquals(22, bikes.size)
        assertEquals(22, bikes.map { it.id }.toSet().size)
        assertTrue(bikes.first().price == 0)
        assertTrue(bikes.last().maxTopSpeedKmh > bikes.first().maxTopSpeedKmh)
    }

    @Test
    fun raceEngineAcceleratesSteersAndAccumulatesHighSpeedBonus() {
        val starter = BikeUiModel(
            spec = BikeCatalog.byId("apex_gp_proto"),
            isUnlocked = true,
            isSelected = true,
            speedLevel = 1,
            handlingLevel = 1,
            brakingLevel = 1
        )
        val engine = RaceEngine(
            bike = starter,
            map = HighwayMap.NIGHT_CITY,
            trafficMode = TrafficMode.TWO_WAY
        )

        val initialSpeed = engine.speedKmh
        // Clear vehicles briefly so test simulates pure acceleration/steering cleanly
        engine.vehicles.clear()

        repeat(120) {
            engine.step(
                dtSec = 1f / 60f,
                throttlePressed = true,
                brakePressed = false,
                steerInput = 0.5f
            )
        }

        assertTrue("Expected speed > 100 km/h, was ${engine.speedKmh}", engine.speedKmh > 100f)
        assertTrue("Expected acceleration from $initialSpeed", engine.speedKmh > initialSpeed)
        assertTrue("Expected high-speed seconds > 0", engine.highSpeedSeconds > 0f)
        assertTrue("Expected distance > 0", engine.distanceMeters > 0f)
        assertTrue("Expected playerX within road bounds", engine.playerX <= RaceEngine.ROAD_LIMIT_X)

        val speedBeforeBrake = engine.speedKmh
        repeat(45) {
            engine.step(
                dtSec = 1f / 60f,
                throttlePressed = false,
                brakePressed = true,
                steerInput = 0f
            )
        }
        assertTrue("Expected braking to reduce speed", engine.speedKmh < speedBeforeBrake)
    }
}
