package com.example.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val units: String = "metric",
    val language: String = "en"
)

interface UserPreferencesDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun setUnits(units: String)
    suspend fun setLanguage(language: String)
}

class UserPreferencesDataSourceImpl(
    private val context: Context
) : UserPreferencesDataSource {

    private object Keys {
        val UNITS    = stringPreferencesKey("units")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override val userPreferences: Flow<UserPreferences> =
        context.prefsDataStore.data.map { prefs ->
            UserPreferences(
                units    = prefs[Keys.UNITS]    ?: "metric",
                language = prefs[Keys.LANGUAGE] ?: "en"
            )
        }

    override suspend fun setUnits(units: String) {
        context.prefsDataStore.edit { it[Keys.UNITS] = units }
    }

    override suspend fun setLanguage(language: String) {
        context.prefsDataStore.edit { it[Keys.LANGUAGE] = language }
    }
}