package com.example.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.data.repository.CityRepository
import com.example.weather.data.remote.mapper.WeatherMapper
import com.example.weather.data.remote.dto.CurrentWeatherDto
import com.example.weather.data.remote.dto.ForecastResponseDto
import com.example.weather.data.repository.WeatherResult
import com.example.weather.location.LocationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CityPagerViewModel(
    private val cityRepository: CityRepository,
    private val prefsSource: UserPreferencesDataSource,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _pages = MutableStateFlow<List<CityEntity>>(emptyList())
    val pages: StateFlow<List<CityEntity>> = _pages.asStateFlow()

    private val _states = MutableStateFlow<Map<Int, HomeUiState>>(emptyMap())
    val states: StateFlow<Map<Int, HomeUiState>> = _states.asStateFlow()

    private val _currentPage = MutableStateFlow(0)

    private val fetchJobs = mutableMapOf<Int, Job>()

    private var suppressNextEmission = false

    init {
        viewModelScope.launch {
            cityRepository.cities.collect { entities ->
                if (suppressNextEmission) {
                    suppressNextEmission = false
                    _pages.value = entities
                    return@collect
                }

                val previousIds = _pages.value.map { it.id }.toSet()
                _pages.value = entities

                entities.forEach { entity ->
                    val state = _states.value[entity.id]
                    val isNew = entity.id !in previousIds
                    when {
                        isNew -> fetchForCity(entity)
                        state == null -> fetchForCity(entity)
                        state is HomeUiState.Error -> fetchForCity(entity)
                        state is HomeUiState.Success -> { /* keep existing data */
                        }

                        state is HomeUiState.Loading -> { /* already in-flight  */
                        }
                    }
                }
            }
        }
    }

    fun onPageSelected(index: Int) {
        _currentPage.value = index
        val entity = _pages.value.getOrNull(index) ?: return
        val state = _states.value[entity.id]
        if (state == null || state is HomeUiState.Error) fetchForCity(entity)
    }

    fun refreshPage(cityId: Int) {
        _pages.value.firstOrNull { it.id == cityId }?.let { fetchForCity(it) }
    }

    fun refreshAll() = _pages.value.forEach { fetchForCity(it) }

    private fun fetchForCity(entity: CityEntity) {
        fetchJobs[entity.id]?.cancel()
        fetchJobs[entity.id] = viewModelScope.launch {
            setPageState(entity.id, HomeUiState.Loading)

            try {
                val prefs = prefsSource.userPreferences.first()

                val lat: Double
                val lon: Double
                if (entity.isCurrentLocation) {
                    val loc =
                        locationProvider.getLastLocation() ?: locationProvider.getCurrentLocation()
                        ?: throw Exception("Unable to get current location")
                    lat = loc.latitude
                    lon = loc.longitude
                } else {
                    lat = entity.lat
                    lon = entity.lon
                }

                val result =
                    cityRepository.fetchWeatherResult(lat, lon, prefs.units, prefs.language)
                        ?: throw Exception("Could not load weather for ${entity.name}")

                val current: CurrentWeatherDto
                val forecast: ForecastResponseDto
                val isFromCache: Boolean
                val cachedAt: Long
                when (result) {
                    is WeatherResult.Live -> {
                        current = result.current
                        forecast = result.forecast
                        isFromCache = false
                        cachedAt = 0L
                    }

                    is WeatherResult.Cached -> {
                        current = result.current
                        forecast = result.forecast
                        isFromCache = true
                        cachedAt = result.cachedAtEpochMs
                    }
                }

                val uiState = WeatherMapper.toHomeUiState(current, forecast, prefs.units)
                    .copy(isFromCache = isFromCache, cachedAtEpochMs = cachedAt)

                setPageState(entity.id, uiState)

                if (entity.isCurrentLocation) {
                    suppressNextEmission = true
                    cityRepository.upsertCurrentLocation(
                        name = uiState.cityName, lat = lat, lon = lon
                    )
                }

            } catch (e: Exception) {
                setPageState(entity.id, HomeUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private fun setPageState(cityId: Int, state: HomeUiState) {
        _states.update { it + (cityId to state) }
    }


    class Factory(
        private val cityRepository: CityRepository,
        private val prefsSource: UserPreferencesDataSource,
        private val locationProvider: LocationProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CityPagerViewModel::class.java)) return CityPagerViewModel(
                cityRepository,
                prefsSource,
                locationProvider
            ) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}