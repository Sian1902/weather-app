package com.example.weather.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.data.local.cities.CityRepository
import com.example.weather.data.local.cities.WeatherSnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CityUiItem(
    val entity   : CityEntity,
    val snapshot : WeatherSnapshot? = null,
    val loading  : Boolean          = true
)

class CitiesViewModel(
    private val cityRepo   : CityRepository,
    private val prefsSource: UserPreferencesDataSource
) : ViewModel() {

    private val _items = MutableStateFlow<List<CityUiItem>>(emptyList())
    val items: StateFlow<List<CityUiItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            cityRepo.cities.collect { entities ->
                _items.value = entities.map { CityUiItem(it) }
                val prefs = prefsSource.userPreferences.first()
                entities.forEach { entity ->
                    launch {
                        // Localization Fix: Ensure the weather snapshot is fetched
                        // using the saved language preference.
                        val snapshot = cityRepo.fetchWeatherSnapshot(
                            lat   = entity.lat,
                            lon   = entity.lon,
                            units = prefs.units,
                            lang  = prefs.language
                        )
                        _items.update { list ->
                            list.map { item ->
                                if (item.entity.id == entity.id)
                                    item.copy(snapshot = snapshot, loading = false)
                                else item
                            }
                        }
                    }
                }
            }
        }
    }

    fun addCity(city: CityEntity) {
        viewModelScope.launch { cityRepo.addCity(city) }
    }

    fun deleteCity(city: CityEntity) {
        viewModelScope.launch { cityRepo.deleteCity(city) }
    }

    /**
     * Moves the default flag to [city].
     * Only one city can be default at a time — repository clears all others first.
     */
    fun setDefault(city: CityEntity) {
        viewModelScope.launch { cityRepo.setDefault(city.id) }
    }

    /**
     * Called from AppRoot whenever a fresh GPS fix is available so the
     * current-location row stays up to date.
     */
    fun upsertCurrentLocation(name: String, lat: Double, lon: Double) {
        viewModelScope.launch { cityRepo.upsertCurrentLocation(name, lat, lon) }
    }

    class Factory(
        private val cityRepo   : CityRepository,
        private val prefsSource: UserPreferencesDataSource
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CitiesViewModel::class.java))
                return CitiesViewModel(cityRepo, prefsSource) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}