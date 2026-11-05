package com.cst3115.enterprise.weatherly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cst3115.enterprise.weatherly.data.CurrentWeatherEntity
import com.cst3115.enterprise.weatherly.data.DailyForecastEntity
import com.cst3115.enterprise.weatherly.viewmodel.WeatherUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val Navy = Color(0xFF0B1220)
private val CardNavy = Color(0xFF152238)
private val Muted = Color(0xFFB4C3D8)
private val Accent = Color(0xFF93C5FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    Scaffold(
        containerColor = Navy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Weatherly",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "A little clarity for your day.",
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSettings) {
                        Text("Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR WEATHER",
                        color = Accent,
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp
                    )

                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !state.isLoading
                    ) {
                        Text(
                            if (state.isLoading) "Updating…" else "Refresh"
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Accent
                    )
                }
            }

            state.error?.let { message ->
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            val current = state.current

            if (current != null) {
                item {
                    CurrentWeatherCard(
                        weather = current,
                        fahrenheit = state.useFahrenheit,
                        saved = state.isSavedData
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailCard(
                            title = "Humidity",
                            value = "${current.humidity}%",
                            modifier = Modifier.weight(1f)
                        )

                        DetailCard(
                            title = "Wind",
                            value = "${(current.windSpeed * 3.6).roundToInt()} km/h",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "5-day outlook",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily summaries from available forecast intervals",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                    }
                }

                val cityToday = Instant.now()
                    .atOffset(
                        ZoneOffset.ofTotalSeconds(current.timezoneOffset)
                    )
                    .toLocalDate()

                val visibleForecast = state.forecast.filter {
                    LocalDate.parse(it.date).isAfter(cityToday)
                }

                if (visibleForecast.isEmpty()) {
                    item {
                        Text(
                            text = "No upcoming forecast saved. Refresh when connected.",
                            color = Muted
                        )
                    }
                }

                items(
                    items = visibleForecast,
                    key = { it.date }
                ) { day ->
                    ForecastCard(
                        day = day,
                        fahrenheit = state.useFahrenheit
                    )
                }

                item {
                    Text(
                        text = "Weather data by OpenWeather",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted
                    )
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CardNavy
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("☁", fontSize = 48.sp)

                            Text(
                                text = if (state.isLoading) {
                                    "Checking the skies…"
                                } else {
                                    "Weather unavailable"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Selected city: ${state.city}",
                                color = Muted
                            )

                            Text(
                                text = "Use Settings to choose another city.",
                                color = Muted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    weather: CurrentWeatherEntity,
    fahrenheit: Boolean,
    saved: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF234F91),
                        Color(0xFF172E55)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = weather.country,
                        color = Color(0xFFD4E5FF)
                    )
                }

                Text(
                    text = weatherSymbol(weather.conditionId),
                    fontSize = 46.sp
                )
            }

            Text(
                text = temperature(weather.temperature, fahrenheit),
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            Text(
                text = weather.description.replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Text(
                text = "Feels like ${
                    temperature(weather.feelsLike, fahrenheit)
                }",
                color = Color(0xFFD4E5FF)
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f)
            )

            val savedTime = Instant.ofEpochMilli(weather.savedAt)
                .atZone(ZoneId.systemDefault())
                .format(
                    DateTimeFormatter.ofPattern(
                        "MMM d, h:mm a",
                        Locale.getDefault()
                    )
                )

            Text(
                text = if (saved) {
                    "Saved weather • $savedTime"
                } else {
                    "Updated $savedTime"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFD4E5FF)
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ForecastCard(
    day: DailyForecastEntity,
    fahrenheit: Boolean
) {
    val dateLabel = LocalDate.parse(day.date).format(
        DateTimeFormatter.ofPattern(
            "EEE, MMM d",
            Locale.getDefault()
        )
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardNavy
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = weatherSymbol(day.conditionId),
                    fontSize = 28.sp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = day.description,
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low ${
                        temperature(day.minTemperature, fahrenheit)
                    }  /  High ${
                        temperature(day.maxTemperature, fahrenheit)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Peak rain chance: ${
                    (day.rainChance * 100).roundToInt()
                }%",
                color = Accent,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initialCity: String,
    initialFahrenheit: Boolean,
    onBack: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var city by rememberSaveable {
        mutableStateOf(initialCity)
    }

    var fahrenheit by rememberSaveable {
        mutableStateOf(initialFahrenheit)
    }

    var showCityError by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = Navy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Make it yours.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Choose your city and preferred temperature unit.",
                    color = Muted
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardNavy
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = {
                            city = it
                            showCityError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("City") },
                        placeholder = { Text("Ottawa,CA") },
                        singleLine = true,
                        isError = showCityError,
                        supportingText = {
                            Text(
                                if (showCityError) {
                                    "Please enter a city."
                                } else {
                                    "Try Ottawa,CA or Nairobi,KE."
                                }
                            )
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardNavy
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Use Fahrenheit",
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (fahrenheit) {
                                "Temperature shown in °F"
                            } else {
                                "Temperature shown in °C"
                            },
                            color = Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = fahrenheit,
                        onCheckedChange = { fahrenheit = it }
                    )
                }
            }

            Button(
                onClick = {
                    if (city.isBlank()) {
                        showCityError = true
                    } else {
                        onSave(city.trim(), fahrenheit)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save preferences",
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Your preferences are remembered when you reopen the app.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun temperature(
    celsius: Double,
    fahrenheit: Boolean
): String {
    val value = if (fahrenheit) {
        celsius * 9.0 / 5.0 + 32
    } else {
        celsius
    }

    val unit = if (fahrenheit) "°F" else "°C"

    return "${value.roundToInt()}$unit"
}

private fun weatherSymbol(conditionId: Int): String {
    return when (conditionId) {
        in 200..232 -> "⛈"
        in 300..321 -> "🌦"
        in 500..531 -> "🌧"
        in 600..622 -> "❄"
        in 701..781 -> "🌫"
        800 -> "☀"
        801 -> "🌤"
        in 802..804 -> "☁"
        else -> "🌡"
    }
}