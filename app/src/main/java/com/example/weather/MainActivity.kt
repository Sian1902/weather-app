package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.data.remote.RetrofitClient
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.repository.WeatherRepositoryImpl
import com.example.weather.ui.home.HomeScreen
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HomeViewModel
import com.example.weather.ui.theme.WeatherColors
import com.example.weather.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey     = BuildConfig.WEATHER_API_KEY
        val dataSource = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)
        val repository = WeatherRepositoryImpl(dataSource)
        val factory    = HomeViewModel.Factory(repository)

        setContent {
            WeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        WeatherRoot(factory = factory)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherRoot(factory: HomeViewModel.Factory) {
    val viewModel: HomeViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadWeatherByCityName("Cairo")
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingScreen()

        is HomeUiState.Error   -> ErrorScreen(
            message   = state.message,
            onRetry   = { viewModel.loadWeatherByCityName("Cairo") }
        )

        is HomeUiState.Success -> HomeScreen(
            cityName             = state.cityName,
            currentTemp          = state.currentTemp,
            highTemp             = state.highTemp,
            lowTemp              = state.lowTemp,
            weatherDescription   = state.weatherDescription,
            feelsLike            = state.feelsLike,
            actualTemp           = state.actualTemp,
            feelsLikeDescription = state.feelsLikeDescription,
            uvIndex              = state.uvIndex,
            uvLabel              = state.uvLabel,
            uvDescription        = state.uvDescription,
            windSpeedBft         = state.windSpeedBft,
            windDeg              = state.windDeg,
            windDescription      = state.windDescription,
            humidity             = state.humidity,
            humidityDescription  = state.humidityDescription,
            visibilityKm         = state.visibilityKm,
            visibilityDescription= state.visibilityDescription,
            pressure             = state.pressure,
            pressureDescription  = state.pressureDescription,
            sunriseTime          = state.sunriseTime,
            sunsetTime           = state.sunsetTime,
            moonPhase            = state.moonPhase,
            hourlyItems          = state.hourlyItems,
            dailyItems           = state.dailyItems
        )
    }
}
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WeatherColors.SkyTop, WeatherColors.SkyDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WeatherColors.SkyTop, WeatherColors.SkyDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text      = "⚠️  Couldn't load weather",
                color     = Color.White,
                fontSize  = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text      = message,
                color     = Color.White.copy(alpha = 0.7f),
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text("Retry", color = Color.White)
            }
        }
    }
}