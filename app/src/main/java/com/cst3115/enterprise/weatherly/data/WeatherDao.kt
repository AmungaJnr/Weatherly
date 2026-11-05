// The DAO contains our database operations.

package com.cst3115.enterprise.weatherly.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class WeatherDao {

    @Query("""
        SELECT * FROM current_weather
        WHERE cityKey = :cityKey
        LIMIT 1
    """)
    abstract suspend fun getCurrentWeather(
        cityKey: String
    ): CurrentWeatherEntity?

    @Query("""
        SELECT * FROM daily_forecast
        WHERE cityKey = :cityKey
        ORDER BY date ASC
    """)
    abstract suspend fun getForecast(
        cityKey: String
    ): List<DailyForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCurrentWeather(
        weather: CurrentWeatherEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertForecast(
        forecast: List<DailyForecastEntity>
    )

    @Query("DELETE FROM daily_forecast WHERE cityKey = :cityKey")
    abstract suspend fun deleteForecast(cityKey: String)

    @Transaction
    open suspend fun saveWeather(
        current: CurrentWeatherEntity,
        forecast: List<DailyForecastEntity>
    ) {
        insertCurrentWeather(current)
        deleteForecast(current.cityKey)
        insertForecast(forecast)
    }

}