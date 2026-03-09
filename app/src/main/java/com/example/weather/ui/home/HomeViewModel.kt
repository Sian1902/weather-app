package com.example.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.mapper.WeatherMapper
import com.example.weather.data.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    fun loadWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {

                val currentDeferred  = async {
                    repository.getCurrentWeatherByCoordinates(lat, lon, units, lang)
                }
                val forecastDeferred = async {
                    repository.getForecastByCoordinates(lat, lon, units, lang)
                }

                val current  = currentDeferred.await()
                val forecast = forecastDeferred.await()

                _uiState.value = WeatherMapper.toHomeUiState(current, forecast)

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Load weather by city name (search / favourites).
     */
    fun loadWeatherByCityName(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val currentDeferred  = async {
                    repository.getCurrentWeatherByCityName(cityName, units, lang)
                }
                val forecastDeferred = async {
                    repository.getForecastByCityName(cityName, units, lang)
                }

                val current  = currentDeferred.await()
                val forecast = forecastDeferred.await()

                _uiState.value = WeatherMapper.toHomeUiState(current, forecast)

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /** Convenience – reload with the same parameters. */
    fun refresh() {
        val current = _uiState.value
        if (current is HomeUiState.Success) {
            loadWeatherByCityName(current.cityName)
        }
    }

    // ──────────────────────────────────────────────
    // Factory
    // ──────────────────────────────────────────────
    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}