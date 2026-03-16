package com.example.weather.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.repository.CityRepository
import com.example.weather.data.repository.WeatherSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CityUiItem(
    val entity: CityEntity, val snapshot: WeatherSnapshot? = null, val loading: Boolean = true
)

class CitiesViewModel(
    private val cityRepo: CityRepository, private val prefsSource: UserPreferencesDataSource
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

                        val snapshot = cityRepo.fetchWeatherSnapshot(
                            lat = entity.lat,
                            lon = entity.lon,
                            units = prefs.units,
                            lang = prefs.language
                        )
                        _items.update { list ->
                            list.map { item ->
                                if (item.entity.id == entity.id) item.copy(
                                    snapshot = snapshot, loading = false
                                )
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

    fun setDefault(city: CityEntity) {
        viewModelScope.launch { cityRepo.setDefault(city.id) }
    }


    class Factory(
        private val cityRepo: CityRepository, private val prefsSource: UserPreferencesDataSource
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CitiesViewModel::class.java)) return CitiesViewModel(
                cityRepo, prefsSource
            ) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}