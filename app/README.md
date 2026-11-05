# Weatherly

Weatherly is an Android weather application developed for CST3115 Lab 4.
It displays current weather and a five-day forecast for a selected city.

## Features

- Modern dark interface built with Jetpack Compose and Material 3.
- Current temperature, weather description, and feels-like temperature.
- Humidity and wind speed.
- Five-day forecast summaries with low/high temperatures and peak
  precipitation probability.
- Settings screen for changing the city and temperature unit.
- Navigation between the weather and Settings screens.
- Manual weather refresh and loading indicators.
- Celsius/Fahrenheit conversion.
- Persistent city and temperature preferences using SharedPreferences.
- Local storage of current weather and forecasts using Room.
- Saved weather displayed when an update fails or the app opens offline.
- Error messages for invalid cities, connection failures, and API errors.

## Technologies

- Kotlin
- Jetpack Compose and Material 3
- Navigation Compose
- Retrofit with Gson
- Room Database with KSP
- SharedPreferences
- ViewModel, StateFlow, and Kotlin coroutines
- OpenWeather API

## Project Structure

- data: API models, Retrofit client, Room entities, DAO, database,
  preferences, and repository.
- viewmodel: Weather UI state and loading/settings logic.
- ui.screens: Weather dashboard and Settings screen.
- MainActivity: Application entry point, theme, and navigation.

## Setup

1. Open the project in Android Studio.
2. Add an OpenWeather API key to the root local.properties file:

   WEATHER_API_KEY=YOUR_API_KEY

3. Sync Gradle and run on a device or emulator with API 27 or higher.

## Forecast Processing

The API provides forecast intervals every three hours.
The repository groups these by the selected city's local date.

Each daily summary displays:
- Minimum and maximum temperatures from available intervals.
- A representative weather description from the interval closest to noon.
- The highest precipitation probability among that day's intervals.

The final forecast day may contain partial coverage.
Temperatures are stored in Celsius and converted locally for Fahrenheit.

## Verification

Manually verified:
- Current weather and forecast loading.
- City changes and Celsius/Fahrenheit conversion.
- Preferences retained after restarting.
- Saved weather available offline.
- Invalid-city and empty-input handling.