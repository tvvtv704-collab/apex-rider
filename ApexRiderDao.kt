package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApexRiderDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM bike_ownership")
    fun observeBikes(): Flow<List<BikeOwnershipEntity>>

    @Query("SELECT * FROM bike_ownership WHERE bikeId = :bikeId")
    suspend fun getBikeOnce(bikeId: String): BikeOwnershipEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBikesIfAbsent(bikes: List<BikeOwnershipEntity>)

    @Update
    suspend fun updateBike(bike: BikeOwnershipEntity)

    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC LIMIT 30")
    fun observeRecentRides(): Flow<List<RideHistoryEntity>>

    @Insert
    suspend fun insertRide(ride: RideHistoryEntity)
}
