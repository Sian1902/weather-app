package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.weather.data.local.WeatherLocalDataSourceImpl
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.local.prefs.UserPreferencesDataSourceImpl
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.remote.api.RetrofitClient
import com.example.weather.data.repository.CityRepository
import com.example.weather.data.repository.WeatherRepositoryImpl
import com.example.weather.location.LocationProviderImpl
import com.example.weather.ui.home.CityPagerViewModel
import com.example.weather.ui.home.HomeScreen
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HomeViewModel
import com.example.weather.ui.settings.SettingsScreen
import com.example.weather.ui.settings.SettingsViewModel
import com.example.weather.ui.theme.WeatherColors
import com.example.weather.ui.theme.WeatherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var homeFactory: HomeViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val apiKey = BuildConfig.WEATHER_API_KEY
        val dataSource = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)
        val localSource = WeatherLocalDataSourceImpl(applicationContext)
        val prefsDataSource = UserPreferencesDataSourceImpl(applicationContext)
        val repository = WeatherRepositoryImpl(dataSource, localSource)
        val locationProvider = LocationProviderImpl(applicationContext)

        val cityDao =
            com.example.weather.data.local.cities.CityDatabase.getInstance(applicationContext)
                .cityDao()
        val cityRepository = CityRepository(
            dao = cityDao, remote = dataSource, weatherRepo = repository, apiKey = apiKey
        )

        val initialLanguage = runBlocking { prefsDataSource.userPreferences.first().language }

        homeFactory = HomeViewModel.Factory(
            repository = repository,
            locationProvider = locationProvider,
            prefsDataSource = prefsDataSource
        )

        setContent {
            var language by remember { mutableStateOf(initialLanguage) }

            val localizedConfig = remember(language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration).also { it.setLocale(locale) }
                @Suppress("DEPRECATION") resources.updateConfiguration(
                    config, resources.displayMetrics
                )
                config
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalLayoutDirection provides if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
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
                        AppRoot(
                            homeFactory = homeFactory,
                            prefsDataSource = prefsDataSource,
                            appContext = applicationContext,
                            cityRepository = cityRepository,
                            locationProvider = locationProvider,
                            onLanguageChange = { newLang -> language = newLang })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    homeFactory: HomeViewModel.Factory,
    prefsDataSource: UserPreferencesDataSource,
    appContext: android.content.Context,
    cityRepository: CityRepository,
    locationProvider: com.example.weather.location.LocationProvider,
    onLanguageChange: (String) -> Unit
) {
    val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)
    var showSettings by remember { mutableStateOf(false) }
    var showCities by remember { mutableStateOf(false) }

    val settingsFactory = remember(homeViewModel) {
        SettingsViewModel.Factory(
            prefsDataSource = prefsDataSource,
            appContext = appContext,
            onLanguageChanged = onLanguageChange,
            onUnitsToggled = { homeViewModel.toggleUnits() })
    }

    val citiesFactory = remember {
        com.example.weather.ui.cities.CitiesViewModel.Factory(
            cityRepo = cityRepository, prefsSource = prefsDataSource
        )
    }

    val pagerFactory = remember {
        CityPagerViewModel.Factory(
            cityRepository = cityRepository,
            prefsSource = prefsDataSource,
            locationProvider = locationProvider
        )
    }

    val citiesViewModel: com.example.weather.ui.cities.CitiesViewModel =
        viewModel(factory = citiesFactory)

    val pagerViewModel: CityPagerViewModel = viewModel(factory = pagerFactory)

    when {
        showSettings -> {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { showSettings = false },
                onLanguageChange = onLanguageChange
            )
        }

        showCities -> {
            com.example.weather.ui.cities.ManageCitiesScreen(
                viewModel = citiesViewModel,
                onBack = { showCities = false },
                onOpenCity = { city ->
                    val idx = pagerViewModel.pages.value.indexOfFirst { it.id == city.id }
                    if (idx >= 0) pagerViewModel.onPageSelected(idx)
                    showCities = false
                },
                onSetDefault = { city ->
                    val idx = pagerViewModel.pages.value.indexOfFirst { it.id == city.id }
                    if (idx >= 0) pagerViewModel.onPageSelected(idx)
                    showCities = false
                })
        }

        else -> {
            WeatherRoot(
                homeViewModel = homeViewModel,
                pagerViewModel = pagerViewModel,
                cityRepository = cityRepository,
                onSettingsClick = { showSettings = true },
                onCitiesClick = { showCities = true })
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WeatherRoot(
    homeViewModel: HomeViewModel,
    pagerViewModel: CityPagerViewModel,
    cityRepository: CityRepository,
    onSettingsClick: () -> Unit,
    onCitiesClick: () -> Unit
) {
    val context = LocalContext.current
    val pages by pagerViewModel.pages.collectAsState()
    val states by pagerViewModel.states.collectAsState()
    val units by homeViewModel.units.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            homeViewModel.onLocationPermissionGranted()
            pagerViewModel.refreshAll()
        } else {
            homeViewModel.onLocationPermissionDenied()
        }
    }

    var startupDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (startupDone) return@LaunchedEffect
        startupDone = true
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            homeViewModel.onLocationPermissionGranted()
            pagerViewModel.refreshAll()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (pages.isEmpty()) {
        val uiState by homeViewModel.uiState.collectAsState()
        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingScreen()
            is HomeUiState.Error -> ErrorScreen(state.message) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    homeViewModel.onLocationPermissionGranted()
                    pagerViewModel.refreshAll()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

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
                isRefreshing = false,
                isFromCache = state.isFromCache,
                cachedAtEpochMs = state.cachedAtEpochMs,
                units = units,
                onRefresh = { homeViewModel.refresh() },
                onUnitsToggle = { homeViewModel.toggleUnits() },
                onSettingsClick = onSettingsClick,
                onMenuClick = onCitiesClick
            )
        }
        return
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0, pageCount = { pages.size })

    LaunchedEffect(pagerState.currentPage) {
        pagerViewModel.onPageSelected(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val entity = pages.getOrNull(pageIndex)
            val pageState = entity?.let { states[it.id] } ?: HomeUiState.Loading

            when (val state = pageState) {
                is HomeUiState.Loading -> LoadingScreen()
                is HomeUiState.Error -> ErrorScreen(state.message) {
                    entity?.let { pagerViewModel.refreshPage(it.id) }
                }

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
                    isRefreshing = false,
                    isFromCache = state.isFromCache,
                    cachedAtEpochMs = state.cachedAtEpochMs,
                    units = units,
                    pageCount = pages.size,
                    currentPage = pageIndex,
                    onRefresh = { entity?.let { pagerViewModel.refreshPage(it.id) } },
                    onUnitsToggle = { homeViewModel.toggleUnits(); pagerViewModel.refreshAll() },
                    onSettingsClick = onSettingsClick,
                    onMenuClick = onCitiesClick
                )
            }
        }
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
            Text(
                stringResource(R.string.error_title),
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text(stringResource(R.string.error_retry), color = Color.White)
            }
        }
    }
}