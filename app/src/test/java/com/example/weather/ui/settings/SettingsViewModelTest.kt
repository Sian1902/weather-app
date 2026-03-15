package com.example.weather.ui.settings

import android.content.Context
import com.example.weather.data.local.UserPreferences
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.worker.WeatherAlarmReceiver
import com.example.weather.worker.WeatherNotificationReceiver
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
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val prefsDataSource   = mockk<UserPreferencesDataSource>()
    private val appContext        = mockk<Context>(relaxed = true)
    private val onLanguageChanged = mockk<(String) -> Unit>(relaxed = true)
    private val onUnitsToggled    = mockk<() -> Unit>(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(WeatherNotificationReceiver)
        mockkObject(WeatherAlarmReceiver)

        coEvery { prefsDataSource.userPreferences } returns flowOf(UserPreferences())
        coEvery { prefsDataSource.setLanguage(any()) }              just Runs
        coEvery { prefsDataSource.setNotificationsEnabled(any()) }  just Runs
        coEvery { prefsDataSource.setNotificationTime(any(), any()) } just Runs
        coEvery { prefsDataSource.setAlarmEnabled(any()) }          just Runs
        coEvery { prefsDataSource.setAlarmTime(any(), any()) }      just Runs
        every  { WeatherNotificationReceiver.scheduleDaily(any(), any(), any()) } returns true
        every  { WeatherNotificationReceiver.cancel(any()) }        just Runs
        every  { WeatherAlarmReceiver.scheduleDaily(any(), any(), any()) } returns true
        every  { WeatherAlarmReceiver.cancel(any()) }               just Runs

        viewModel = SettingsViewModel(
            prefsDataSource   = prefsDataSource,
            appContext        = appContext,
            onLanguageChanged = onLanguageChanged,
            onUnitsToggled    = onUnitsToggled
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(WeatherNotificationReceiver)
        unmockkObject(WeatherAlarmReceiver)
    }

    // ── Test 1: setLanguage persists and calls the callback ───────────────────

    @Test
    fun `setLanguage persists to DataStore and invokes onLanguageChanged callback`() = runTest {
        viewModel.setLanguage("ar")
        advanceUntilIdle()

        coVerify { prefsDataSource.setLanguage("ar") }
        verify   { onLanguageChanged("ar") }
    }

    // ── Test 2: toggleUnits delegates to the injected lambda ─────────────────

    @Test
    fun `toggleUnits calls the onUnitsToggled lambda`() {
        viewModel.toggleUnits()
        verify(exactly = 1) { onUnitsToggled() }
    }

    // ── Test 3: toggleNotifications enables and schedules the notification ────

    @Test
    fun `toggleNotifications enables notifications and schedules alarm`() = runTest {
        // Arrange — notifications are off by default in UserPreferences()
        coEvery { prefsDataSource.userPreferences } returns flowOf(
            UserPreferences(notificationsEnabled = false, notificationHour = 7, notificationMinute = 0)
        )

        viewModel.toggleNotifications()
        advanceUntilIdle()

        coVerify { prefsDataSource.setNotificationsEnabled(true) }
        verify   { WeatherNotificationReceiver.scheduleDaily(appContext, 7, 0) }
        // needsExactAlarmPermission should be false because scheduleDaily returned true
        assertFalse(viewModel.needsExactAlarmPermission.value)
    }
}