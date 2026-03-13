package com.example.weather.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.UserPreferences
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.worker.WeatherNotificationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsDataSource  : UserPreferencesDataSource,
    private val appContext       : Context,
    private val onLanguageChanged: (String) -> Unit,
    private val onUnitsToggled   : () -> Unit
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = prefsDataSource.userPreferences
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    // True when Android 12+ "Alarms & reminders" permission is not granted.
    // UI shows a banner + "Grant" button when this is true.
    private val _needsExactAlarmPermission = MutableStateFlow(false)
    val needsExactAlarmPermission: StateFlow<Boolean> = _needsExactAlarmPermission.asStateFlow()

    // ── Language ──────────────────────────────────────────────────────────────

    fun setLanguage(language: String) {
        viewModelScope.launch { prefsDataSource.setLanguage(language) }
        onLanguageChanged(language)
    }

    // ── Units ─────────────────────────────────────────────────────────────────

    fun toggleUnits() = onUnitsToggled()

    // ── Notifications ─────────────────────────────────────────────────────────

    fun toggleNotifications() {
        viewModelScope.launch {
            // Read fresh from DataStore — avoids StateFlow stale-value race
            val current  = prefsDataSource.userPreferences.first()
            val enabling = !current.notificationsEnabled
            prefsDataSource.setNotificationsEnabled(enabling)

            if (enabling) {
                val exactOk = WeatherNotificationReceiver.scheduleDaily(
                    appContext, current.notificationHour, current.notificationMinute
                )
                _needsExactAlarmPermission.value = !exactOk
            } else {
                WeatherNotificationReceiver.cancel(appContext)
                _needsExactAlarmPermission.value = false
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsDataSource.setNotificationTime(hour, minute)
            val current = prefsDataSource.userPreferences.first()
            if (current.notificationsEnabled) {
                val exactOk = WeatherNotificationReceiver.scheduleDaily(appContext, hour, minute)
                _needsExactAlarmPermission.value = !exactOk
            }
        }
    }

    /** Called from the UI "Grant" button — opens system Alarms & reminders screen. */
    fun openExactAlarmSettings() {
        WeatherNotificationReceiver.openExactAlarmSettings(appContext)
    }

    /** Call when the user returns from system settings so we re-check the permission. */
    fun recheckExactAlarmPermission() {
        if (!WeatherNotificationReceiver.canScheduleExact(appContext)) return
        _needsExactAlarmPermission.value = false
        // Re-schedule with exact alarm now that permission is granted
        viewModelScope.launch {
            val prefs = prefsDataSource.userPreferences.first()
            if (prefs.notificationsEnabled) {
                WeatherNotificationReceiver.scheduleDaily(
                    appContext, prefs.notificationHour, prefs.notificationMinute
                )
            }
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val prefsDataSource  : UserPreferencesDataSource,
        private val appContext       : Context,
        private val onLanguageChanged: (String) -> Unit,
        private val onUnitsToggled   : () -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java))
                return SettingsViewModel(
                    prefsDataSource   = prefsDataSource,
                    appContext        = appContext,
                    onLanguageChanged = onLanguageChanged,
                    onUnitsToggled    = onUnitsToggled
                ) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}