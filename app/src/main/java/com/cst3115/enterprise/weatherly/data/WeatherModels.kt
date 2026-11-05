package com.cst3115.enterprise.weatherly.data

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    val name: String,
    val main: WeatherMeasurements,
    val weather: List<WeatherCondition>,
    val wind: Wind,
    val sys: CountryInfo,
    val timezone: Int,
    val dt: Long
)

data class WeatherMeasurements(
    val temp: Double,

    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerializedName("temp_min")
    val tempMin: Double,

    @SerializedName("temp_max")
    val tempMax: Double,

    val humidity: Int
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class CountryInfo(
    val country: String
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: ForecastCity
)

data class ForecastItem(
    val dt: Long,
    val main: WeatherMeasurements,
    val weather: List<WeatherCondition>,
    val pop: Double = 0.0
)

data class ForecastCity(
    val name: String,
    val country: String,
    val timezone: Int
)