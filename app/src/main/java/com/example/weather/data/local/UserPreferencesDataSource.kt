package com.example.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val units                : String  = "metric",   // "metric" | "imperial"
    val language             : String  = "en",        // "en" | "ar"
    val notificationsEnabled : Boolean = false,
    val notificationHour     : Int     = 7,           // 0–23
    val notificationMinute   : Int     = 0            // 0–59
)

interface UserPreferencesDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setUnits(units: String)
    suspend fun setLanguage(language: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setNotificationTime(hour: Int, minute: Int)
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
    }

    override val userPreferences: Flow<UserPreferences> =
        context.prefsDataStore.data.map { prefs ->
            UserPreferences(
                units                = prefs[Keys.UNITS]               ?: "metric",
                language             = prefs[Keys.LANGUAGE]            ?: "en",
                notificationsEnabled = prefs[Keys.NOTIFICATIONS]       ?: false,
                notificationHour     = prefs[Keys.NOTIFICATION_HOUR]   ?: 7,
                notificationMinute   = prefs[Keys.NOTIFICATION_MINUTE] ?: 0
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
}