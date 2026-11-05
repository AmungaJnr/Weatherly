/*
This groups forecast intervals by the selected city’s local date.
The displayed low/high will summarize the available intervals.
The final forecast day may have partial coverage.
 */

package com.cst3115.enterprise.weatherly.data

import androidx.room.withTransaction
import com.cst3115.enterprise.weatherly.BuildConfig
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.abs

data class WeatherData(
    val current: CurrentWeatherEntity,
    val forecast: List<DailyForecastEntity>
)

class WeatherRepository (
    private val database: WeatherDatabase
) {
    private val dao = database.weatherDao()
    private val api = RetrofitClient.api

    fun cityKey(city: String): String {
        return city.trim().lowercase(Locale.ROOT)
    }

    suspend fun getSavedWeather(city: String): WeatherData? {
        val key = cityKey(city)

        return database.withTransaction {
            val current = dao.getCurrentWeather(key)

            if (current == null) {
                null
            } else {
                WeatherData(
                    current = current,
                    forecast = dao.getForecast(key)
                )
            }
        }
    }

    suspend fun refreshWeather(city: String): WeatherData {
        check(BuildConfig.WEATHER_API_KEY.isNotBlank()) {
            "Add your weather API key to local.properties and rebuild."
        }

        val currentResponse = api.getCurrentWeather(
            city = city.trim(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )

        val forecastResponse = api.getForecast(
            city = city.trim(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )

        val key = cityKey(city)
        val condition = currentResponse.weather.firstOrNull()

        val current = CurrentWeatherEntity(
            cityKey = key,
            cityName = currentResponse.name,
            country = currentResponse.sys.country,
            temperature = currentResponse.main.temp,
            feelsLike = currentResponse.main.feelsLike,
            humidity = currentResponse.main.humidity,
            windSpeed = currentResponse.wind.speed,
            description = condition?.description ?: "Unavailable",
            conditionId = condition?.id ?: 0,
            icon = condition?.icon ?: "",
            timezoneOffset = currentResponse.timezone,
            observedAt = currentResponse.dt,
            savedAt = System.currentTimeMillis()
        )

        val offset = ZoneOffset.ofTotalSeconds(
            forecastResponse.city.timezone
        )

        val today = Instant.ofEpochSecond(currentResponse.dt)
            .atOffset(offset)
            .toLocalDate()

        val forecast = forecastResponse.list
            .groupBy { item ->
                Instant.ofEpochSecond(item.dt)
                    .atOffset(offset)
                    .toLocalDate()
            }
            .filterKeys { date -> date.isAfter(today) }
            .toSortedMap()
            .entries
            .take(5)
            .map { (date, intervals) ->

                // Choose the interval closest to local noon
                // for the day's representative description.
                val representative = intervals.minByOrNull { item ->
                    val time = Instant.ofEpochSecond(item.dt)
                        .atOffset(offset)
                        .toLocalTime()

                    abs(time.toSecondOfDay() - 12 * 60 * 60)
                }!!

                val dailyCondition =
                    representative.weather.firstOrNull()

                DailyForecastEntity(
                    cityKey = key,
                    date = date.toString(),
                    minTemperature = intervals.minOf {
                        it.main.tempMin
                    },
                    maxTemperature = intervals.maxOf {
                        it.main.tempMax
                    },
                    description = dailyCondition?.description
                        ?: "Unavailable",
                    conditionId = dailyCondition?.id ?: 0,
                    icon = dailyCondition?.icon ?: "",
                    rainChance = intervals.maxOf { it.pop }
                )
            }

        dao.saveWeather(current, forecast)

        return WeatherData(
            current = current,
            forecast = forecast
        )
    }
}
