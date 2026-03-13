package com.example.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val units                : String  = "metric",
    val language             : String  = "en",
    // ── Daily notification (silent, posted to status bar) ──────────────────
    val notificationsEnabled : Boolean = false,
    val notificationHour     : Int     = 7,
    val notificationMinute   : Int     = 0,
    // ── Daily alarm (plays sound / vibrates to wake user) ──────────────────
    val alarmEnabled         : Boolean = false,
    val alarmHour            : Int     = 7,
    val alarmMinute          : Int     = 0,
    // ── Last known location persisted for the workers ──────────────────────
    val lastLat              : Double? = null,
    val lastLon              : Double? = null,
    val lastCityName         : String  = "Cairo"
)

interface UserPreferencesDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setUnits(units: String)
    suspend fun setLanguage(language: String)
    // Notification
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setNotificationTime(hour: Int, minute: Int)
    // Alarm
    suspend fun setAlarmEnabled(enabled: Boolean)
    suspend fun setAlarmTime(hour: Int, minute: Int)
    // Location
    suspend fun setLastLocationCoords(lat: Double, lon: Double)
    suspend fun setLastLocationCity(cityName: String)
}

class UserPreferencesDataSourceImpl(
    private val context: Context
) : UserPreferencesDataSource {

    private object Keys {
        val UNITS               = stringPreferencesKey("units")
        val LANGUAGE            = stringPreferencesKey("language")
        val NOTIFICATIONS       = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR   = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val ALARM_ENABLED       = booleanPreferencesKey("alarm_enabled")
        val ALARM_HOUR          = intPreferencesKey("alarm_hour")
        val ALARM_MINUTE        = intPreferencesKey("alarm_minute")
        val LAST_LAT            = doublePreferencesKey("last_lat")
        val LAST_LON            = doublePreferencesKey("last_lon")
        val LAST_CITY           = stringPreferencesKey("last_city")
    }

    override val userPreferences: Flow<UserPreferences> =
        context.prefsDataStore.data.map { prefs ->
            UserPreferences(
                units                = prefs[Keys.UNITS]               ?: "metric",
                language             = prefs[Keys.LANGUAGE]            ?: "en",
                notificationsEnabled = prefs[Keys.NOTIFICATIONS]       ?: false,
                notificationHour     = prefs[Keys.NOTIFICATION_HOUR]   ?: 7,
                notificationMinute   = prefs[Keys.NOTIFICATION_MINUTE] ?: 0,
                alarmEnabled         = prefs[Keys.ALARM_ENABLED]       ?: false,
                alarmHour            = prefs[Keys.ALARM_HOUR]          ?: 7,
                alarmMinute          = prefs[Keys.ALARM_MINUTE]        ?: 0,
                lastLat              = prefs[Keys.LAST_LAT],
                lastLon              = prefs[Keys.LAST_LON],
                lastCityName         = prefs[Keys.LAST_CITY]           ?: "Cairo"
            )
        }

    override suspend fun setUnits(units: String) {
        context.prefsDataStore.edit { it[Keys.UNITS] = units }
    }

    override suspend fun setLanguage(language: String) {
        context.prefsDataStore.edit { it[Keys.LANGUAGE] = language }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.prefsDataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.prefsDataStore.edit {
            it[Keys.NOTIFICATION_HOUR]   = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        context.prefsDataStore.edit { it[Keys.ALARM_ENABLED] = enabled }
    }

    override suspend fun setAlarmTime(hour: Int, minute: Int) {
        context.prefsDataStore.edit {
            it[Keys.ALARM_HOUR]   = hour
            it[Keys.ALARM_MINUTE] = minute
        }
    }

    override suspend fun setLastLocationCoords(lat: Double, lon: Double) {
        context.prefsDataStore.edit {
            it[Keys.LAST_LAT] = lat
            it[Keys.LAST_LON] = lon
        }
    }

    override suspend fun setLastLocationCity(cityName: String) {
        context.prefsDataStore.edit { it[Keys.LAST_CITY] = cityName }
    }
}