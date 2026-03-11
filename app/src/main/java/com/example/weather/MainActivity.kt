package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.data.local.WeatherLocalDataSourceImpl
import com.example.weather.data.remote.RetrofitClient
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.repository.WeatherRepositoryImpl
import com.example.weather.location.LocationProviderImpl
import com.example.weather.ui.home.HomeScreen
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HomeViewModel
import com.example.weather.ui.theme.WeatherColors
import com.example.weather.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey           = BuildConfig.WEATHER_API_KEY
        val dataSource       = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)
        val localSource      = WeatherLocalDataSourceImpl(applicationContext)
        val repository       = WeatherRepositoryImpl(dataSource, localSource)
        val locationProvider = LocationProviderImpl(applicationContext)
        val factory          = HomeViewModel.Factory(repository, locationProvider)

        setContent {
            WeatherTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    WeatherColors.SkyTop,
                                    WeatherColors.SkyMid,
                                    WeatherColors.SkyDeep,
                                    WeatherColors.SkyBottom
                                )
                            )
                        )
                ) {
                    WeatherRoot(factory = factory)
                }
            }
        }
    }
}

@Composable
private fun WeatherRoot(factory: HomeViewModel.Factory) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val context = LocalContext.current

    val uiState      by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onLocationPermissionGranted()
        else         viewModel.onLocationPermissionDenied()
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (granted) viewModel.onLocationPermissionGranted()
        else permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingScreen()

        is HomeUiState.Error -> ErrorScreen(
            message = state.message,
            onRetry = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) viewModel.onLocationPermissionGranted()
                else permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )

        is HomeUiState.Success -> HomeScreen(
            cityName              = state.cityName,
            countryCode           = state.countryCode,      // ← new
            currentTemp           = state.currentTemp,
            highTemp              = state.highTemp,
            lowTemp               = state.lowTemp,
            weatherDescription    = state.weatherDescription,
            feelsLike             = state.feelsLike,
            actualTemp            = state.actualTemp,
            feelsLikeDescription  = state.feelsLikeDescription,
            uvIndex               = state.uvIndex,
            uvLabel               = state.uvLabel,
            uvDescription         = state.uvDescription,
            windSpeedBft          = state.windSpeedBft,
            windDeg               = state.windDeg,
            windDescription       = state.windDescription,
            humidity              = state.humidity,
            humidityDescription   = state.humidityDescription,
            visibilityKm          = state.visibilityKm,
            visibilityDescription = state.visibilityDescription,
            pressure              = state.pressure,
            pressureDescription   = state.pressureDescription,
            sunriseTime           = state.sunriseTime,
            sunsetTime            = state.sunsetTime,
            moonPhase             = state.moonPhase,
            hourlyItems           = state.hourlyItems,
            dailyItems            = state.dailyItems,
            isRefreshing          = isRefreshing,
            isFromCache           = state.isFromCache,
            cachedAtEpochMs       = state.cachedAtEpochMs,
            onRefresh             = { viewModel.refresh() }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("⚠️  Couldn't load weather", color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
            Text(message, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text("Retry", color = Color.White)
            }
        }
    }
}