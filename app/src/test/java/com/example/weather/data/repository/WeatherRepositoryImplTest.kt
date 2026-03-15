package com.example.weather.data.repository

import com.example.weather.TestFixtures
import com.example.weather.data.local.WeatherLocalDataSource
import com.example.weather.data.remote.WeatherRemoteDataSource
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    private val remote     = mockk<WeatherRemoteDataSource>()
    private val local      = mockk<WeatherLocalDataSource>()
    private val gson       = Gson()

    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(remote, local, gson)
    }

    // ── Test 1: getWeatherWithCache returns Live on network success ───────────

    @Test
    fun getWeatherWithCache_networkSuccess_returnsLiveResult() = runTest {
        coEvery {
            remote.getCurrentWeatherByCityName("Cairo", "metric", "en")
        } returns TestFixtures.currentWeatherDto

        coEvery {
            remote.getForecastByCityName("Cairo", "metric", "en")
        } returns TestFixtures.forecastResponseDto

        coEvery { local.saveWeatherCache(any(), any()) } just Runs

        val result = repository.getWeatherWithCache("Cairo", "metric", "en")

        assertTrue("Expected Live result", result is WeatherResult.Live)
        assertEquals("Cairo", (result as WeatherResult.Live).current.name)
    }

    // ── Test 2: getWeatherWithCache falls back to cache on network failure ────

    @Test
    fun getWeatherWithCache_networkFails_returnsCachedResult() = runTest {
        // Network throws
        coEvery {
            remote.getCurrentWeatherByCityName(any(), any(), any())
        } throws Exception("No internet")

        // Cache returns serialized DTO pair
        val cachedJson = gson.toJson(
            mapOf(
                "current"          to TestFixtures.currentWeatherDto,
                "forecast"         to TestFixtures.forecastResponseDto,
                "cachedAtEpochMs"  to 1_700_000_000L
            )
        )
        coEvery { local.loadWeatherCache("Cairo") } returns cachedJson

        // WeatherRepositoryImpl loads cache via loadCachedResult — mock correctly
        // The implementation calls loadWeatherCache with the cityName key
        val result = repository.getWeatherWithCache("Cairo", "metric", "en")

        assertTrue("Expected Cached result on network failure", result is WeatherResult.Cached)
    }

    // ── Test 3: getWeatherWithCacheByCoords uses coords as cache key ──────────

    @Test
    fun getWeatherWithCacheByCoords_callsRemoteWithCorrectParams() = runTest {
        coEvery {
            remote.getCurrentWeatherByCoordinates(30.06, 31.23, "metric", "en")
        } returns TestFixtures.currentWeatherDto

        coEvery {
            remote.getForecastByCoordinates(30.06, 31.23, "metric", "en")
        } returns TestFixtures.forecastResponseDto

        coEvery { local.saveWeatherCache(any(), any()) } just Runs

        val result = repository.getWeatherWithCacheByCoords(30.06, 31.23, "metric", "en")

        assertTrue(result is WeatherResult.Live)
        coVerify {
            remote.getCurrentWeatherByCoordinates(30.06, 31.23, "metric", "en")
            remote.getForecastByCoordinates(30.06, 31.23, "metric", "en")
        }
    }
}