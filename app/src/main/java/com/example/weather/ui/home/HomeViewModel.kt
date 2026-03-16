package com.example.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.remote.mapper.WeatherMapper
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.data.repository.WeatherResult
import com.example.weather.location.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val prefsDataSource: UserPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    private val _units = MutableStateFlow("metric")
    val units: StateFlow<String> = _units.asStateFlow()


    private var currentLang: String = "en"

    private var lastRequest: LastRequest? = null

    private var prefsLoaded = false

    init {
        viewModelScope.launch {
            val prefs = prefsDataSource.userPreferences.first()
            _units.value = prefs.units
            currentLang = prefs.language
            prefsLoaded = true
        }
    }

    sealed class LastRequestSnapshot {
        data class ByCoords(val lat: Double, val lon: Double) : LastRequestSnapshot()
        object ByCity : LastRequestSnapshot()
        object None : LastRequestSnapshot()
    }


    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            while (!prefsLoaded) kotlinx.coroutines.delay(10)

            _uiState.value = HomeUiState.Loading
            try {

                val loc =
                    locationProvider.getLastLocation() ?: locationProvider.getCurrentLocation()

                if (loc != null) {
                    lastRequest =
                        LastRequest.ByCoords(loc.latitude, loc.longitude, _units.value, currentLang)
                    fetchByCoords(loc.latitude, loc.longitude, _units.value, currentLang)
                } else {
                    _uiState.value =
                        HomeUiState.Error("Unable to get your location. Please make sure GPS is enabled and try again.")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unable to get location.")
            }
        }
    }

    fun onLocationPermissionDenied() {
        viewModelScope.launch {
            while (!prefsLoaded) kotlinx.coroutines.delay(10)
            _uiState.value =
                HomeUiState.Error("Location permission is required to show your local weather.")
        }
    }

    fun toggleUnits() {
        val newUnits = if (_units.value == "metric") "imperial" else "metric"
        _units.value = newUnits
        viewModelScope.launch { prefsDataSource.setUnits(newUnits) }

        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity -> {
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

    fun refresh() {
        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity -> fetchByCityName(req.cityName, req.units, req.lang)
                is LastRequest.ByCoords -> fetchByCoords(req.lat, req.lon, req.units, req.lang)
            }
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchByCityName(cityName: String, units: String, lang: String) {
        try {
            _uiState.value = repository.getWeatherWithCache(cityName, units, lang).toUiState()
            prefsDataSource.setLastLocationCity(cityName)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchByCoords(lat: Double, lon: Double, units: String, lang: String) {
        try {
            _uiState.value =
                repository.getWeatherWithCacheByCoords(lat, lon, units, lang).toUiState()
            prefsDataSource.setLastLocationCoords(lat, lon)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }
    }

    private fun WeatherResult.toUiState(): HomeUiState.Success = when (this) {
        is WeatherResult.Live -> WeatherMapper.toHomeUiState(current, forecast, _units.value)
            .copy(isFromCache = false)

        is WeatherResult.Cached -> WeatherMapper.toHomeUiState(current, forecast, _units.value)
            .copy(isFromCache = true, cachedAtEpochMs = cachedAtEpochMs)
    }

    private sealed class LastRequest {
        data class ByCity(
            val cityName: String, val units: String = "metric", val lang: String = "en"
        ) : LastRequest()

        data class ByCoords(
            val lat: Double, val lon: Double, val units: String = "metric", val lang: String = "en"
        ) : LastRequest()
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationProvider: LocationProvider,
        private val prefsDataSource: UserPreferencesDataSource
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) return HomeViewModel(
                repository,
                locationProvider,
                prefsDataSource
            ) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}