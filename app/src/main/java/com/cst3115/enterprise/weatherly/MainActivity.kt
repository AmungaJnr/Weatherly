package com.cst3115.enterprise.weatherly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cst3115.enterprise.weatherly.ui.screens.SettingsScreen
import com.cst3115.enterprise.weatherly.ui.screens.WeatherScreen
import com.cst3115.enterprise.weatherly.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF93C5FD),
                    onPrimary = Color(0xFF102542),
                    background = Color(0xFF0B1220),
                    onBackground = Color(0xFFF1F5F9),
                    surface = Color(0xFF152238),
                    onSurface = Color(0xFFF1F5F9),
                    onSurfaceVariant = Color(0xFFB4C3D8),
                    outline = Color(0xFF64748B)
                )
            ) {
                WeatherlyApp()
            }
        }
    }
}

@Composable
fun WeatherlyApp(
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by weatherViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "weather"
    ) {
        composable("weather") {
            WeatherScreen(
                state = state,
                onRefresh = weatherViewModel::refresh,
                onSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                initialCity = state.city,
                initialFahrenheit = state.useFahrenheit,
                onBack = {
                    navController.popBackStack()
                },
                onSave = { city, fahrenheit ->
                    if (weatherViewModel.saveSettings(city, fahrenheit)) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}