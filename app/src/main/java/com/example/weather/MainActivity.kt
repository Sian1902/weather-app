package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.data.local.UserPreferencesDataSourceImpl
import com.example.weather.data.local.WeatherLocalDataSourceImpl
import com.example.weather.data.remote.RetrofitClient
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.repository.WeatherRepositoryImpl
import com.example.weather.location.LocationProviderImpl
import com.example.weather.ui.home.HomeScreen
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HomeViewModel
import com.example.weather.ui.settings.SettingsScreen
import com.example.weather.ui.settings.SettingsViewModel
import com.example.weather.ui.theme.WeatherColors
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var homeFactory: HomeViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Dependencies
        val apiKey = BuildConfig.WEATHER_API_KEY
        val dataSource = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)
        val localSource = WeatherLocalDataSourceImpl(applicationContext)
        val prefsDataSource = UserPreferencesDataSourceImpl(applicationContext)
        val repository = WeatherRepositoryImpl(dataSource, localSource)
        val locationProvider = LocationProviderImpl(applicationContext)

        // Get initial language for state
        val initialLanguage = runBlocking { prefsDataSource.userPreferences.first().language }

        homeFactory = HomeViewModel.Factory(
            repository = repository,
            locationProvider = locationProvider,
            prefsDataSource = prefsDataSource
        )

        setContent {
            val activityContext = LocalContext.current // The actual Activity
            var language by remember { mutableStateOf(initialLanguage) }

            // Create localized context for strings/layout
            val localizedContext = remember(language) {
                LocaleHelper.applyLocale(activityContext, language)
            }

            // Create Settings Factory
            val settingsFactory = remember(prefsDataSource) {
                SettingsViewModel.Factory(
                    prefsDataSource = prefsDataSource,
                    onLanguageChanged = { newLang -> language = newLang }
                )
            }

            // Provide localized context for strings, but keep activity for Registry
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalLayoutDirection provides if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
                // FIX: Re-provide the original activity as the Registry Owner
                LocalActivityResultRegistryOwner provides (activityContext as ComponentActivity)
            ) {
                WeatherTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        WeatherColors.SkyTop, WeatherColors.SkyMid,
                                        WeatherColors.SkyDeep, WeatherColors.SkyBottom
                                    )
                                )
                            )
                    ) {
                        AppRoot(
                            homeFactory = homeFactory,
                            settingsFactory = settingsFactory,
                            currentLanguage = language,
                            onLanguageChange = { language = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    homeFactory: HomeViewModel.Factory,
    settingsFactory: SettingsViewModel.Factory,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
        SettingsScreen(
            viewModel = settingsViewModel,
            onBack = { showSettings = false },
            onLanguageChange = onLanguageChange
        )
    } else {
        WeatherRoot(
            homeFactory = homeFactory,
            currentLanguage = currentLanguage,
            onSettingsClick = { showSettings = true }
        )
    }
}

@Composable
private fun WeatherRoot(
    homeFactory: HomeViewModel.Factory,
    currentLanguage: String,
    onSettingsClick: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = homeFactory)
    val context = LocalContext.current // This is localizedContext from provider

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val units by viewModel.units.collectAsState()

    LaunchedEffect(currentLanguage) {
        viewModel.onLanguageChanged(currentLanguage)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onLocationPermissionGranted()
        else viewModel.onLocationPermissionDenied()
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
        is HomeUiState.Error -> ErrorScreen(message = state.message, onRetry = { viewModel.refresh() })
        is HomeUiState.Success -> HomeScreen(
            cityName = state.cityName,
            countryCode = state.countryCode,
            currentTemp = state.currentTemp,
            unitSymbol = state.unitSymbol,
            highTemp = state.highTemp,
            lowTemp = state.lowTemp,
            weatherDescription = state.weatherDescription,
            feelsLike = state.feelsLike,
            actualTemp = state.actualTemp,
            feelsLikeRaw = state.feelsLikeRaw,
            actualTempRaw = state.actualTempRaw,
            uvIndex = state.uvIndex,
            windSpeedBft = state.windSpeedBft,
            windDeg = state.windDeg,
            humidity = state.humidity,
            visibilityKm = state.visibilityKm,
            pressure = state.pressure,
            sunriseTime = state.sunriseTime,
            sunsetTime = state.sunsetTime,
            moonPhaseRaw = state.moonPhaseRaw,
            hourlyItems = state.hourlyItems,
            dailyItems = state.dailyItems,
            isRefreshing = isRefreshing,
            isFromCache = state.isFromCache,
            cachedAtEpochMs = state.cachedAtEpochMs,
            units = units,
            onRefresh = { viewModel.refresh() },
            onUnitsToggle = { viewModel.toggleUnits() },
            onSettingsClick = onSettingsClick
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
            Text(stringResource(R.string.error_title), color = Color.White, fontSize = 18.sp)
            Text(message, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text(stringResource(R.string.error_retry), color = Color.White)
            }
        }
    }
}