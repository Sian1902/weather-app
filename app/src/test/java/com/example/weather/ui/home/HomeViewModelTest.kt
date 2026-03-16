package com.example.weather.ui.home

import app.cash.turbine.test
import com.example.weather.TestFixtures
import com.example.weather.data.local.prefs.UserPreferences
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.location.LocationProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = mockk<WeatherRepository>()
    private val locationProvider = mockk<LocationProvider>()
    private val prefsDataSource = mockk<UserPreferencesDataSource>()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { prefsDataSource.userPreferences } returns flowOf(UserPreferences())
        coEvery { prefsDataSource.setLanguage(any()) } just Runs
        coEvery { prefsDataSource.setUnits(any()) } just Runs
        coEvery { prefsDataSource.setLastLocationCoords(any(), any()) } just Runs
        coEvery { prefsDataSource.setLastLocationCity(any()) } just Runs

        viewModel = HomeViewModel(repository, locationProvider, prefsDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLocationPermissionGranted emits Success when GPS returns a location`() = runTest {
        val mockLocation = mockk<android.location.Location> {
            every { latitude } returns 30.06
            every { longitude } returns 31.23
        }
        coEvery { locationProvider.getLastLocation() } returns mockLocation
        coEvery {
            repository.getWeatherWithCacheByCoords(
                any(),
                any(),
                any(),
                any()
            )
        } returns TestFixtures.liveResult

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())   // initial state

            viewModel.onLocationPermissionGranted()

            val result = awaitItem()
            assertTrue("Expected Success but got $result", result is HomeUiState.Success)
            assertEquals("Cairo", (result as HomeUiState.Success).cityName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLocationPermissionDenied emits Error state`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onLocationPermissionDenied()

            val result = awaitItem()
            assertTrue(result is HomeUiState.Error)
            assertTrue(
                (result as HomeUiState.Error).message.contains(
                    "permission",
                    ignoreCase = true
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleUnits switches from metric to imperial and re-fetches`() = runTest {

        val mockLocation = mockk<android.location.Location> {
            every { latitude } returns 30.06
            every { longitude } returns 31.23
        }
        coEvery { locationProvider.getLastLocation() } returns mockLocation
        coEvery {
            repository.getWeatherWithCacheByCoords(
                any(),
                any(),
                any(),
                any()
            )
        } returns TestFixtures.liveResult

        viewModel.onLocationPermissionGranted()
        advanceUntilIdle()

        assertEquals("metric", viewModel.units.value)

        viewModel.toggleUnits()
        advanceUntilIdle()

        assertEquals("imperial", viewModel.units.value)
        coVerify {
            repository.getWeatherWithCacheByCoords(30.06, 31.23, "imperial", any())
        }
    }
}