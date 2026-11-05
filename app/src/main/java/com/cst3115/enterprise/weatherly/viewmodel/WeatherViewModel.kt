package com.cst3115.enterprise.weatherly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cst3115.enterprise.weatherly.data.CurrentWeatherEntity
import com.cst3115.enterprise.weatherly.data.DailyForecastEntity
import com.cst3115.enterprise.weatherly.data.WeatherDatabase
import com.cst3115.enterprise.weatherly.data.WeatherPreferences
import com.cst3115.enterprise.weatherly.data.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class WeatherUiState(
    val city: String = "Ottawa,CA",
    val useFahrenheit: Boolean = false,
    val isLoading: Boolean = false,
    val current: CurrentWeatherEntity? = null,
    val forecast: List<DailyForecastEntity> = emptyList(),
    val isSavedData: Boolean = false,
    val error: String? = null
)

class WeatherViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val preferences = WeatherPreferences(application)

    private val repository = WeatherRepository(
        WeatherDatabase.getDatabase(application)
    )

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            city = preferences.city,
            useFahrenheit = preferences.useFahrenheit
        )
    )

    val uiState = _uiState.asStateFlow()

    private var weatherJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        weatherJob?.cancel()

        val city = _uiState.value.city
        val key = repository.cityKey(city)

        _uiState.update { state ->
            val sameCity = state.current?.cityKey == key

            state.copy(
                isLoading = true,
                error = null,
                current = if (sameCity) state.current else null,
                forecast = if (sameCity) state.forecast else emptyList(),
                isSavedData = sameCity && state.isSavedData
            )
        }

        weatherJob = viewModelScope.launch {
            // Show saved weather while requesting an update.
            try {
                val saved = repository.getSavedWeather(city)

                if (saved != null) {
                    _uiState.update {
                        it.copy(
                            current = saved.current,
                            forecast = saved.forecast,
                            isSavedData = true
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // A cache read failure should not stop an API request.
            }

            try {
                val fresh = repository.refreshWeather(city)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        current = fresh.current,
                        forecast = fresh.forecast,
                        isSavedData = false,
                        error = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { state ->
                    val hasSavedWeather = state.current != null

                    state.copy(
                        isLoading = false,
                        isSavedData = hasSavedWeather,
                        error = errorMessage(exception) +
                                if (hasSavedWeather) {
                                    " Showing previously saved weather."
                                } else {
                                    ""
                                }
                    )
                }
            }
        }
    }

    fun saveSettings(
        city: String,
        useFahrenheit: Boolean
    ): Boolean {
        val cleanedCity = city.trim()

        if (cleanedCity.isBlank()) {
            return false
        }

        val cityChanged =
            repository.cityKey(cleanedCity) !=
                    repository.cityKey(_uiState.value.city)

        preferences.saveSettings(cleanedCity, useFahrenheit)

        _uiState.update {
            it.copy(
                city = cleanedCity,
                useFahrenheit = useFahrenheit
            )
        }

        if (cityChanged) {
            refresh()
        }

        return true
    }

    private fun errorMessage(exception: Exception): String {
        return when (exception) {
            is HttpException -> when (exception.code()) {
                401 -> "API key rejected. Check your key and its activation."
                404 -> "City not found. Try a name such as Ottawa,CA."
                429 -> "Weather request limit reached. Try again later."
                else -> "Weather service error. Please try again."
            }

            is IOException ->
                "Could not connect. Check your internet connection."

            is IllegalStateException ->
                exception.message ?: "Check your app configuration."

            else ->
                "Unable to load or save weather. Please try again."
        }
    }
}
