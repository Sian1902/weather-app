package com.example.weather.ui.settings

import android.content.Context
import com.example.weather.data.local.prefs.UserPreferences
import com.example.weather.data.local.prefs.UserPreferencesDataSource
import com.example.weather.data.local.worker.WeatherAlarmReceiver
import com.example.weather.data.local.worker.WeatherNotificationReceiver
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val prefsDataSource = mockk<UserPreferencesDataSource>()
    private val appContext = mockk<Context>(relaxed = true)
    private val onLanguageChanged = mockk<(String) -> Unit>(relaxed = true)
    private val onUnitsToggled = mockk<() -> Unit>(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(WeatherNotificationReceiver)
        mockkObject(WeatherAlarmReceiver)

        coEvery { prefsDataSource.userPreferences } returns flowOf(UserPreferences())
        coEvery { prefsDataSource.setLanguage(any()) } just Runs
        coEvery { prefsDataSource.setNotificationsEnabled(any()) } just Runs
        coEvery { prefsDataSource.setNotificationTime(any(), any()) } just Runs
        coEvery { prefsDataSource.setAlarmEnabled(any()) } just Runs
        coEvery { prefsDataSource.setAlarmTime(any(), any()) } just Runs
        every { WeatherNotificationReceiver.scheduleDaily(any(), any(), any()) } returns true
        every { WeatherNotificationReceiver.cancel(any()) } just Runs
        every { WeatherAlarmReceiver.scheduleDaily(any(), any(), any()) } returns true
        every { WeatherAlarmReceiver.cancel(any()) } just Runs

        viewModel = SettingsViewModel(
            prefsDataSource = prefsDataSource,
            appContext = appContext,
            onLanguageChanged = onLanguageChanged,
            onUnitsToggled = onUnitsToggled
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(WeatherNotificationReceiver)
        unmockkObject(WeatherAlarmReceiver)
    }

    @Test
    fun `setLanguage persists to DataStore and invokes onLanguageChanged callback`() = runTest {
        viewModel.setLanguage("ar")
        advanceUntilIdle()

        coVerify { prefsDataSource.setLanguage("ar") }
        verify { onLanguageChanged("ar") }
    }

    @Test
    fun `toggleUnits calls the onUnitsToggled lambda`() {
        viewModel.toggleUnits()
        verify(exactly = 1) { onUnitsToggled() }
    }

    @Test
    fun `toggleNotifications enables notifications and schedules alarm`() = runTest {
        coEvery { prefsDataSource.userPreferences } returns flowOf(
            UserPreferences(
                notificationsEnabled = false,
                notificationHour = 7,
                notificationMinute = 0
            )
        )

        viewModel.toggleNotifications()
        advanceUntilIdle()

        coVerify { prefsDataSource.setNotificationsEnabled(true) }
        verify { WeatherNotificationReceiver.scheduleDaily(appContext, 7, 0) }
        assertFalse(viewModel.needsExactAlarmPermission.value)
    }
}