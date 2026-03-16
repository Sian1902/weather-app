package com.example.weather.data.repository

import com.example.weather.data.local.cities.CityDao
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.data.remote.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class CityRepository(
    private val dao: CityDao,
    private val remote: WeatherRemoteDataSource,
    private val weatherRepo: WeatherRepository,
    private val apiKey: String
) {
    val cities: Flow<List<CityEntity>> = dao.getAllCities()

    suspend fun addCity(city: CityEntity) = dao.insertCity(city)
    suspend fun deleteCity(city: CityEntity) = dao.deleteCity(city)

    suspend fun setDefault(id: Int) {
        dao.clearAllDefaults()
        dao.setDefaultById(id)
    }

    suspend fun upsertCurrentLocation(name: String, lat: Double, lon: Double) {
        if (dao.currentLocationExists() == 0) {
            dao.insertCity(
                CityEntity(
                    name = name,
                    lat = lat,
                    lon = lon,
                    isCurrentLocation = true,
                    isDefault = dao.getDefaultCity() == null
                )
            )
        } else {
            dao.updateCurrentLocationCity(name, lat, lon, System.currentTimeMillis())
        }
    }


    suspend fun fetchWeatherResult(
        lat: Double, lon: Double, units: String, lang: String
    ): WeatherResult? = runCatching {
        weatherRepo.getWeatherWithCacheByCoords(lat, lon, units, lang)
    }.getOrNull()

    suspend fun fetchWeatherSnapshot(
        lat: Double, lon: Double, units: String, lang: String
    ): WeatherSnapshot? = runCatching {
        val dto = remote.getCurrentWeatherByCoordinates(lat, lon, units, lang)
        val temp = dto.main.temp.toInt()
        val desc = dto.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
        WeatherSnapshot(temp, desc, if (units == "metric") "°C" else "°F")
    }.getOrNull()
}

data class WeatherSnapshot(
    val temp: Int, val desc: String, val unitSymbol: String
)