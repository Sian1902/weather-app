package com.example.weather.data.local.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weather.data.local.prefs.UserPreferencesDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()

                if (prefs.notificationsEnabled) {
                    WeatherNotificationReceiver.scheduleDaily(
                        context, prefs.notificationHour, prefs.notificationMinute
                    )
                }

                if (prefs.alarmEnabled) {
                    WeatherAlarmReceiver.scheduleDaily(
                        context, prefs.alarmHour, prefs.alarmMinute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}