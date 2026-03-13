package com.example.weather.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weather.data.local.UserPreferencesDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reschedules the daily weather alarm after the device reboots.
 * AlarmManager alarms are wiped on reboot — this receiver restores them.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                if (prefs.notificationsEnabled) {
                    WeatherNotificationReceiver.scheduleDaily(
                        context,
                        prefs.notificationHour,
                        prefs.notificationMinute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}