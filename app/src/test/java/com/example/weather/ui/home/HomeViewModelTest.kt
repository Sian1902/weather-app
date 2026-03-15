package com.example.weather.ui.home

import app.cash.turbine.test
import com.example.weather.TestFixtures
import com.example.weather.data.local.UserPreferences
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.data.repository.WeatherResult
import com.example.weather.location.LocationProvider
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // ── Test dispatcher ───────────────────────────────────────────────────────

    private val testDispatcher = UnconfinedTestDispatcher()

    // ── Mocks ─────────────────────────────────────────────────────────────────

    private val repository       = mockk<WeatherRepository>()
    private val locationProvider = mockk<LocationProvider>()
    private val prefsDataSource  = mockk<UserPreferencesDataSource>()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Default prefs returned on every first() call
        coEvery { prefsDataSource.userPreferences } returns flowOf(UserPreferences())
        coEvery { prefsDataSource.setLanguage(any()) } just Runs
        coEvery { prefsDataSource.setUnits(any()) }    just Runs
        coEvery { prefsDataSource.setLastLocationCoords(any(), any()) } just Runs
        coEvery { prefsDataSource.setLastLocationCity(any()) }          just Runs

        viewModel = HomeViewModel(repository, locationProvider, prefsDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Test 1: onLocationPermissionGranted emits Success on GPS fix ──────────

    @Test
    fun `onLocationPermissionGranted emits Success when GPS returns a location`() = runTest {
        // Arrange
        val mockLocation = mockk<android.location.Location> {
            every { latitude }  returns 30.06
            every { longitude } returns 31.23
        }
        coEvery { locationProvider.getLastLocation()      } returns mockLocation
        coEvery { repository.getWeatherWithCacheByCoords(any(), any(), any(), any()) } returns TestFixtures.liveResult

        // Act
        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())   // initial state

            viewModel.onLocationPermissionGranted()

            val result = awaitItem()
            assertTrue("Expected Success but got $result", result is HomeUiState.Success)
            assertEquals("Cairo", (result as HomeUiState.Success).cityName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test 2: onLocationPermissionDenied emits Error ────────────────────────

    @Test
    fun `onLocationPermissionDenied emits Error state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading

            viewModel.onLocationPermissionDenied()

            val result = awaitItem()
            assertTrue(result is HomeUiState.Error)
            assertTrue((result as HomeUiState.Error).message.contains("permission", ignoreCase = true))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Test 3: toggleUnits re-fetches and flips metric ↔ imperial ────────────

    @Test
    fun `toggleUnits switches from metric to imperial and re-fetches`() = runTest {
        // Arrange — seed a successful GPS state first
        val mockLocation = mockk<android.location.Location> {
            every { latitude }  returns 30.06
            every { longitude } returns 31.23
        }
        coEvery { locationProvider.getLastLocation() }   returns mockLocation
        coEvery { repository.getWeatherWithCacheByCoords(any(), any(), any(), any()) } returns TestFixtures.liveResult

        viewModel.onLocationPermissionGranted()
        advanceUntilIdle()

        // Assert initial unit
        assertEquals("metric", viewModel.units.value)

        // Act
        viewModel.toggleUnits()
        advanceUntilIdle()

        // Assert unit flipped and re-fetch was called with "imperial"
        assertEquals("imperial", viewModel.units.value)
        coVerify {
            repository.getWeatherWithCacheByCoords(30.06, 31.23, "imperial", any())
        }
    }
}