package com.example.weather.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.UserPreferences
import com.example.weather.data.local.UserPreferencesDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsDataSource: UserPreferencesDataSource,
    private val onLanguageChanged: (String) -> Unit
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = prefsDataSource.userPreferences
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun setLanguage(language: String) {
        viewModelScope.launch {
            prefsDataSource.setLanguage(language)
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
            // This may trigger an Activity restart or a systematic UI refresh
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    class Factory(
        private val prefsDataSource: UserPreferencesDataSource,
        private val onLanguageChanged: (String) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java))
                return SettingsViewModel(prefsDataSource, onLanguageChanged) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}