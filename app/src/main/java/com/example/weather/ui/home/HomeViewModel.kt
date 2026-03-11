package com.example.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var lastRequest: LastRequest? = null

    // ── Called by MainActivity after permission is confirmed ──────

    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val location = locationProvider.getLastLocation()
                    ?: locationProvider.getCurrentLocation().first()

                lastRequest = LastRequest.ByCoords(location.latitude, location.longitude)
                fetchByCoords(location.latitude, location.longitude)
            } catch (e: Exception) {
                // GPS unavailable — fall back to default city
                loadWeatherByCityName("Cairo")
            }
        }
    }

    fun onLocationPermissionDenied() {
        loadWeatherByCityName("Cairo")
    }

    fun loadWeatherByCityName(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ) {
        lastRequest = LastRequest.ByCity(cityName, units, lang)
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            fetchByCityName(cityName, units, lang)
        }
    }

    // ── Pull-to-refresh ───────────────────────────────────────────

    fun refresh() {
        val req = lastRequest ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (req) {
                is LastRequest.ByCity   ->
                    fetchByCityName(req.cityName, req.units, req.lang)
                is LastRequest.ByCoords ->
                    fetchByCoords(req.lat, req.lon)
            }
            _isRefreshing.value = false
        }
    }

    // ── Internal ─────────────────────────────────────────────────

    private suspend fun fetchByCityName(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ) {
        try {
            _uiState.value = repository
                .getWeatherWithCache(cityName, units, lang)
                .toUiState()
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchByCoords(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ) {
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
            is WeatherResult.Live   -> WeatherMapper.toHomeUiState(current, forecast)
                .copy(isFromCache = false)
            is WeatherResult.Cached -> WeatherMapper.toHomeUiState(current, forecast)
                .copy(isFromCache = true, cachedAtEpochMs = cachedAtEpochMs)
        }

    private sealed class LastRequest {
        data class ByCity(
            val cityName: String,
            val units: String = "metric",
            val lang: String = "en"
        ) : LastRequest()

        data class ByCoords(
            val lat: Double,
            val lon: Double,
            val units: String = "metric",
            val lang: String = "en"
        ) : LastRequest()
    }

    // ── Factory ───────────────────────────────────────────────────

    class Factory(
        private val repository: WeatherRepository,
        private val locationProvider: LocationProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java))
                return HomeViewModel(repository, locationProvider) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}