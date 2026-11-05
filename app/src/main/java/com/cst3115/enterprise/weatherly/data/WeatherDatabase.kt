// This creates one shared database instance.

package com.cst3115.enterprise.weatherly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CurrentWeatherEntity::class,
        DailyForecastEntity::class
    ],
    version = 1,
    exportSchema = false
)

abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    companion object {

        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weatherly_database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}