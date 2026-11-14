package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        BikeOwnershipEntity::class,
        RideHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApexRiderDatabase : RoomDatabase() {
    abstract fun apexRiderDao(): ApexRiderDao

    companion object {
        @Volatile
        private var INSTANCE: ApexRiderDatabase? = null

        fun getInstance(context: Context): ApexRiderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ApexRiderDatabase::class.java,
                    "apex_rider_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
