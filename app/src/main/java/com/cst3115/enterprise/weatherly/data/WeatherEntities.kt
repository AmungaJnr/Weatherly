/*
 These are the tables Room will store.
 They save current weather and daily forecast summaries, with temperatures always stored in Celsius.
 */

package com.cst3115.enterprise.weatherly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(

    @PrimaryKey
    val cityKey: String,

    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val conditionId: Int,
    val icon: String,
    val timezoneOffset: Int,
    val observedAt: Long,
    val savedAt: Long
)

@Entity(
    tableName = "daily_forecast",
    primaryKeys = ["cityKey", "date"]
)
data class DailyForecastEntity(
    val cityKey: String,
    val date: String,
    val minTemperature: Double,
    val maxTemperature: Double,
    val description: String,
    val conditionId: Int,
    val icon: String,
    val rainChance: Double
)
