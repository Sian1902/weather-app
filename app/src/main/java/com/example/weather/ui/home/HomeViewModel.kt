package com.example.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.data.mapper.WeatherMapper
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.data.repository.WeatherResult
import com.example.weather.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository      : WeatherRepository,
    private val locationProvider: LocationProvider,
    private val prefsDataSource : UserPreferencesDataSource
) : ViewModel() {

    private val _uiState     = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _units = MutableStateFlow("metric")
    val units: StateFlow<String> = _units.asStateFlow()

    /**
     * Current language code ("en" | "ar").
     * Stored here so every re-fetch (units toggle, refresh, language switch) uses
     * the same lang without needing to read DataStore again.
     */
    private var currentLang: String = "en"

    private var lastRequest: LastRequest? = null

    init {
        viewModelScope.launch {
            val prefs    = prefsDataSource.userPreferences.first()
            _units.value = prefs.units
            currentLang  = prefs.language
        }
    }

    // ── Location permission callbacks ─────────────────────────────────────────

    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val loc = locationProvider.getLastLocation()
                    ?: locationProvider.getCurrentLocation().first()
                lastRequest = LastRequest.ByCoords(loc.latitude, loc.longitude, _units.value, currentLang)
                fetchByCoords(loc.latitude, loc.longitude, _units.value, currentLang)
            } catch (e: Exception) {
                loadWeatherByCityName("Cairo")
            }
        }
    }

    fun onLocationPermissionDenied() {
        loadWeatherByCityName("Cairo")
    }

    fun loadWeatherByCityName(
        cityName : String,
        units    : String = _units.value,
        lang     : String = currentLang
    ) {
        lastRequest = LastRequest.ByCity(cityName, units, lang)
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            fetchByCityName(cityName, units, lang)
        }
    }

    // ── Language change — called from MainActivity when the user switches ─────
    //
    // The API is called with the new lang so descriptions like "Cloudy" become
    // "غائم".  UI-only strings (card labels, units, etc.) switch instantly
    // because each Composable calls stringResource() which reads from
    // CompositionLocalProvider(LocalConfiguration) — no re-fetch needed for those.

    fun onLanguageChanged(newLang: String) {
        if (newLang == currentLang) return
        currentLang = newLang
        viewModelScope.launch { prefsDataSource.setLanguage(newLang) }

        // Re-fetch so the API returns weatherDescription in the new language
        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity   -> {
                    val updated = req.copy(lang = newLang)
                    lastRequest = updated
                    fetchByCityName(updated.cityName, updated.units, updated.lang)
                }
                is LastRequest.ByCoords -> {
                    val updated = req.copy(lang = newLang)
                    lastRequest = updated
                    fetchByCoords(updated.lat, updated.lon, updated.units, updated.lang)
                }
            }
            _isRefreshing.value = false
        }
    }

    // ── Units toggle ──────────────────────────────────────────────────────────

    fun toggleUnits() {
        val newUnits = if (_units.value == "metric") "imperial" else "metric"
        _units.value = newUnits
        viewModelScope.launch { prefsDataSource.setUnits(newUnits) }

        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity   -> {
                    val updated = req.copy(units = newUnits)
                    lastRequest = updated
                    fetchByCityName(updated.cityName, updated.units, updated.lang)
                }
                is LastRequest.ByCoords -> {
                    val updated = req.copy(units = newUnits)
                    lastRequest = updated
                    fetchByCoords(updated.lat, updated.lon, updated.units, updated.lang)
                }
            }
            _isRefreshing.value = false
        }
    }

    // ── Pull-to-refresh ───────────────────────────────────────────────────────

    fun refresh() {
        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity   -> fetchByCityName(req.cityName, req.units, req.lang)
                is LastRequest.ByCoords -> fetchByCoords(req.lat, req.lon, req.units, req.lang)
            }
            _isRefreshing.value = false
        }
    }

    // ── Internal fetchers ─────────────────────────────────────────────────────

    private suspend fun fetchByCityName(cityName: String, units: String, lang: String) {
        try {
            _uiState.value = repository
                .getWeatherWithCache(cityName, units, lang)
                .toUiState()
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchByCoords(lat: Double, lon: Double, units: String, lang: String) {
        try {
            _uiState.value = repository
                .getWeatherWithCacheByCoords(lat, lon, units, lang)
                .toUiState()
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }
    }

    private fun WeatherResult.toUiState(): HomeUiState.Success =
        when (this) {
            is WeatherResult.Live   ->
                WeatherMapper.toHomeUiState(current, forecast, _units.value)
                    .copy(isFromCache = false)
            is WeatherResult.Cached ->
                WeatherMapper.toHomeUiState(current, forecast, _units.value)
                    .copy(isFromCache = true, cachedAtEpochMs = cachedAtEpochMs)
        }

    // ── Sealed request types ──────────────────────────────────────────────────

    private sealed class LastRequest {
        data class ByCity(
            val cityName : String,
            val units    : String = "metric",
            val lang     : String = "en"
        ) : LastRequest()

        data class ByCoords(
            val lat   : Double,
            val lon   : Double,
            val units : String = "metric",
            val lang  : String = "en"
        ) : LastRequest()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val repository      : WeatherRepository,
        private val locationProvider: LocationProvider,
        private val prefsDataSource : UserPreferencesDataSource
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java))
                return HomeViewModel(repository, locationProvider, prefsDataSource) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}