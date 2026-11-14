package com.example.data.local

import com.example.domain.model.BikeCatalog
import com.example.domain.model.BikeSpec
import com.example.domain.model.BikeUiModel
import com.example.domain.model.HighwayMap
import com.example.domain.model.PlayerState
import com.example.domain.model.RideResult
import com.example.domain.model.SteeringMode
import com.example.domain.model.TrafficMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.max

enum class UpgradeStatType {
    SPEED,
    HANDLING,
    BRAKING
}

class GameRepository(private val dao: ApexRiderDao) {

    suspend fun ensureSeeded() {
        val existingProfile = dao.getProfileOnce()
        if (existingProfile == null) {
            dao.upsertProfile(PlayerProfileEntity())
        }
        val seedBikes = BikeCatalog.allBikes.map { spec ->
            BikeOwnershipEntity(
                bikeId = spec.id,
                isUnlocked = spec.price == 0,
                speedLevel = 1,
                handlingLevel = 1,
                brakingLevel = 1
            )
        }
        dao.insertBikesIfAbsent(seedBikes)
    }

    fun observePlayerState(): Flow<PlayerState> {
        return combine(
            dao.observeProfile(),
            dao.observeBikes()
        ) { profileNullable, ownershipList ->
            val profile = profileNullable ?: PlayerProfileEntity()
            val ownershipMap = ownershipList.associateBy { it.bikeId }

            val bikes = BikeCatalog.allBikes.map { spec ->
                val owned = ownershipMap[spec.id]
                BikeUiModel(
                    spec = spec,
                    isUnlocked = owned?.isUnlocked ?: (spec.price == 0),
                    isSelected = profile.selectedBikeId == spec.id,
                    speedLevel = owned?.speedLevel ?: 1,
                    handlingLevel = owned?.handlingLevel ?: 1,
                    brakingLevel = owned?.brakingLevel ?: 1
                )
            }

            PlayerState(
                cash = profile.cash,
                selectedBikeId = profile.selectedBikeId,
                selectedMap = HighwayMap.byId(profile.selectedMapId),
                steeringMode = runCatching { SteeringMode.valueOf(profile.steeringModeName) }
                    .getOrDefault(SteeringMode.TOUCH_BUTTONS),
                trafficMode = runCatching { TrafficMode.valueOf(profile.trafficModeName) }
                    .getOrDefault(TrafficMode.ONE_WAY),
                bestScore = profile.bestScore,
                bestDistanceKm = profile.bestDistanceKm,
                totalOvertakes = profile.totalOvertakes,
                totalRides = profile.totalRides,
                bikes = bikes
            )
        }
    }

    fun observeRecentRides(): Flow<List<RideHistoryEntity>> = dao.observeRecentRides()

    suspend fun selectMap(map: HighwayMap) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(profile.copy(selectedMapId = map.id))
    }

    suspend fun setSteeringMode(mode: SteeringMode) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(profile.copy(steeringModeName = mode.name))
    }

    suspend fun setTrafficMode(mode: TrafficMode) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(profile.copy(trafficModeName = mode.name))
    }

    suspend fun selectBike(bikeId: String) {
        val owned = dao.getBikeOnce(bikeId)
        if (owned?.isUnlocked == true) {
            val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
            dao.upsertProfile(profile.copy(selectedBikeId = bikeId))
        }
    }

    suspend fun buyBike(spec: BikeSpec): Boolean {
        val profile = dao.getProfileOnce() ?: return false
        val owned = dao.getBikeOnce(spec.id) ?: BikeOwnershipEntity(bikeId = spec.id)
        if (owned.isUnlocked) {
            dao.upsertProfile(profile.copy(selectedBikeId = spec.id))
            return true
        }
        if (profile.cash < spec.price) return false

        dao.upsertProfile(
            profile.copy(
                cash = profile.cash - spec.price,
                selectedBikeId = spec.id
            )
        )
        dao.updateBike(owned.copy(isUnlocked = true))
        return true
    }

    suspend fun upgradeBikeStat(spec: BikeSpec, statType: UpgradeStatType): Boolean {
        val profile = dao.getProfileOnce() ?: return false
        val owned = dao.getBikeOnce(spec.id) ?: return false
        if (!owned.isUnlocked) return false

        val currentLevel = when (statType) {
            UpgradeStatType.SPEED -> owned.speedLevel
            UpgradeStatType.HANDLING -> owned.handlingLevel
            UpgradeStatType.BRAKING -> owned.brakingLevel
        }
        if (currentLevel >= BikeSpec.MAX_UPGRADE_LEVEL) return false

        val cost = spec.upgradeCost(currentLevel)
        if (profile.cash < cost) return false

        dao.upsertProfile(profile.copy(cash = profile.cash - cost))
        val updated = when (statType) {
            UpgradeStatType.SPEED -> owned.copy(speedLevel = currentLevel + 1)
            UpgradeStatType.HANDLING -> owned.copy(handlingLevel = currentLevel + 1)
            UpgradeStatType.BRAKING -> owned.copy(brakingLevel = currentLevel + 1)
        }
        dao.updateBike(updated)
        return true
    }

    suspend fun recordRideResult(result: RideResult) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            profile.copy(
                cash = profile.cash + result.cashEarned,
                bestScore = max(profile.bestScore, result.score),
                bestDistanceKm = max(profile.bestDistanceKm, result.distanceKm),
                totalOvertakes = profile.totalOvertakes + result.overtakes,
                totalRides = profile.totalRides + 1
            )
        )
        dao.insertRide(
            RideHistoryEntity(
                mapId = result.map.id,
                bikeName = result.bikeName,
                score = result.score,
                distanceKm = result.distanceKm,
                topSpeedKmh = result.topSpeedKmh,
                overtakes = result.overtakes,
                cashEarned = result.cashEarned
            )
        )
    }
}
