package com.example.weather.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.UserPreferences
import com.example.weather.data.local.UserPreferencesDataSource
import com.example.weather.worker.WeatherAlarmReceiver
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

    // Separate flags for notification vs alarm — each has its own banner in the UI
    private val _needsExactAlarmPermission       = MutableStateFlow(false)
    val needsExactAlarmPermission: StateFlow<Boolean> = _needsExactAlarmPermission.asStateFlow()

    private val _needsExactAlarmPermissionAlarm  = MutableStateFlow(false)
    val needsExactAlarmPermissionAlarm: StateFlow<Boolean> = _needsExactAlarmPermissionAlarm.asStateFlow()

    // ── Language ──────────────────────────────────────────────────────────────

    fun setLanguage(language: String) {
        viewModelScope.launch { prefsDataSource.setLanguage(language) }
        onLanguageChanged(language)
    }

    // ── Units ─────────────────────────────────────────────────────────────────

    fun toggleUnits() = onUnitsToggled()

    // ── Notification ──────────────────────────────────────────────────────────

    fun toggleNotifications() {
        viewModelScope.launch {
            val current  = prefsDataSource.userPreferences.first()
            val enabling = !current.notificationsEnabled
            prefsDataSource.setNotificationsEnabled(enabling)
            if (enabling) {
                val ok = WeatherNotificationReceiver.scheduleDaily(
                    appContext, current.notificationHour, current.notificationMinute
                )
                _needsExactAlarmPermission.value = !ok
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
                val ok = WeatherNotificationReceiver.scheduleDaily(appContext, hour, minute)
                _needsExactAlarmPermission.value = !ok
            }
        }
    }

    fun openExactAlarmSettings() = WeatherNotificationReceiver.openExactAlarmSettings(appContext)

    fun recheckExactAlarmPermission() {
        if (!WeatherNotificationReceiver.canScheduleExact(appContext)) return
        _needsExactAlarmPermission.value      = false
        _needsExactAlarmPermissionAlarm.value = false
        viewModelScope.launch {
            val prefs = prefsDataSource.userPreferences.first()
            if (prefs.notificationsEnabled)
                WeatherNotificationReceiver.scheduleDaily(appContext, prefs.notificationHour, prefs.notificationMinute)
            if (prefs.alarmEnabled)
                WeatherAlarmReceiver.scheduleDaily(appContext, prefs.alarmHour, prefs.alarmMinute)
        }
    }

    // ── Alarm ─────────────────────────────────────────────────────────────────

    fun toggleAlarm() {
        viewModelScope.launch {
            val current  = prefsDataSource.userPreferences.first()
            val enabling = !current.alarmEnabled
            prefsDataSource.setAlarmEnabled(enabling)
            if (enabling) {
                val ok = WeatherAlarmReceiver.scheduleDaily(
                    appContext, current.alarmHour, current.alarmMinute
                )
                _needsExactAlarmPermissionAlarm.value = !ok
            } else {
                WeatherAlarmReceiver.cancel(appContext)
                _needsExactAlarmPermissionAlarm.value = false
            }
        }
    }

    fun setAlarmTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsDataSource.setAlarmTime(hour, minute)
            val current = prefsDataSource.userPreferences.first()
            if (current.alarmEnabled) {
                val ok = WeatherAlarmReceiver.scheduleDaily(appContext, hour, minute)
                _needsExactAlarmPermissionAlarm.value = !ok
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