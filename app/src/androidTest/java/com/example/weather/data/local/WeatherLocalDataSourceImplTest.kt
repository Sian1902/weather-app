package com.example.weather.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [WeatherLocalDataSourceImpl].
 * Uses a real DataStore backed by the test application context so we verify
 * the actual read/write behaviour, not a mock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WeatherLocalDataSourceTest {

    private lateinit var dataSource: WeatherLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataSource  = WeatherLocalDataSourceImpl(context)
    }

    // ── Test 1: saveWeatherCache then loadWeatherCache returns same JSON ───────

    @Test
    fun saveCache_thenLoad_returnsSameJson() = runTest {
        val city = "TestCity_${System.currentTimeMillis()}"   // unique key per run
        val json = """{"name":"$city","temp":25}"""

        dataSource.saveWeatherCache(city, json)
        val loaded = dataSource.loadWeatherCache(city)

        assertEquals(json, loaded)
    }

    // ── Test 2: loadWeatherCache returns null for unknown city ────────────────

    @Test
    fun loadCache_forUnknownCity_returnsNull() = runTest {
        val result = dataSource.loadWeatherCache("__nonexistent_city__")
        assertNull("Expected null for a key that was never saved", result)
    }

    // ── Test 3: clearCache removes the entry ─────────────────────────────────

    @Test
    fun clearCache_removesEntry_loadReturnsNull() = runTest {
        val city = "ClearTestCity_${System.currentTimeMillis()}"
        dataSource.saveWeatherCache(city, """{"temp":20}""")
        dataSource.clearCache(city)

        val result = dataSource.loadWeatherCache(city)
        assertNull("Expected null after cache was cleared", result)
    }
}