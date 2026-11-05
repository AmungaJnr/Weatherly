package com.cst3115.enterprise.weatherly.data

import android.content.Context

class WeatherPreferences (context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "weatherly_preferences",
            Context.MODE_PRIVATE
        )

    val city: String
        get() = preferences.getString(
            "selected_city",
            "Ottawa,CA"
        ) ?: "Ottawa,CA"

    val useFahrenheit: Boolean
        get() = preferences.getBoolean(
            "use_fahrenheit",
            false
        )

    fun saveSettings(
        city: String,
        useFahrenheit: Boolean
    ) {
        preferences.edit()
            .putString("selected_city", city.trim())
            .putBoolean("use_fahrenheit", useFahrenheit)
            .apply()
    }
}