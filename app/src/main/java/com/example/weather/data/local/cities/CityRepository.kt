package com.example.weather.data.local.cities

import com.example.weather.data.remote.WeatherRemoteDataSource
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.data.repository.WeatherResult
import kotlinx.coroutines.flow.Flow

class CityRepository(
    private val dao            : CityDao,
    private val remote         : WeatherRemoteDataSource,
    private val weatherRepo    : WeatherRepository,   // used for cached fetches
    private val apiKey         : String
) {
    val cities: Flow<List<CityEntity>> = dao.getAllCities()

    suspend fun addCity(city: CityEntity)    = dao.insertCity(city)
    suspend fun deleteCity(city: CityEntity) = dao.deleteCity(city)
    suspend fun getDefaultCity(): CityEntity? = dao.getDefaultCity()

    suspend fun setDefault(id: Int) {
        dao.clearAllDefaults()
        dao.setDefaultById(id)
    }

    suspend fun upsertCurrentLocation(name: String, lat: Double, lon: Double) {
        if (dao.currentLocationExists() == 0) {
            dao.insertCity(
                CityEntity(
                    name              = name,
                    lat               = lat,
                    lon               = lon,
                    isCurrentLocation = true,
                    isDefault         = dao.getDefaultCity() == null
                )
            )
        } else {
            dao.updateCurrentLocationCity(name, lat, lon, System.currentTimeMillis())
        }
    }

    /**
     * Full weather fetch (current + forecast) with caching — used by the pager.
     * Returns null on network failure with no cache available.
     */
    suspend fun fetchWeatherResult(
        lat  : Double,
        lon  : Double,
        units: String,
        lang : String
    ): WeatherResult? = runCatching {
        weatherRepo.getWeatherWithCacheByCoords(lat, lon, units, lang)
    }.getOrNull()

    /** Lightweight snapshot (temp + desc only) — used by city card thumbnails. */
    suspend fun fetchWeatherSnapshot(
        lat  : Double,
        lon  : Double,
        units: String,
        lang : String
    ): WeatherSnapshot? = runCatching {
        val dto  = remote.getCurrentWeatherByCoordinates(lat, lon, units, lang)
        val temp = dto.main.temp.toInt()
        val desc = dto.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""
        WeatherSnapshot(temp, desc, if (units == "metric") "°C" else "°F")
    }.getOrNull()
}

data class WeatherSnapshot(
    val temp      : Int,
    val desc      : String,
    val unitSymbol: String
)