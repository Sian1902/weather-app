package com.example.weather.data.local.worker

import android.R
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.weather.BuildConfig
import com.example.weather.MainActivity
import com.example.weather.data.local.prefs.UserPreferencesDataSourceImpl
import com.example.weather.data.remote.api.RetrofitClient
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * BroadcastReceiver that fires at the exact alarm time and posts the weather notification.
 *
 * Using AlarmManager.setExactAndAllowWhileIdle() instead of WorkManager because:
 * - WorkManager PeriodicWorkRequest has a minimum 15-min interval AND can drift by hours
 *   when the OS batches work — unsuitable for "fire at exactly 07:00".
 * - AlarmManager with setExactAndAllowWhileIdle() fires even in Doze mode (Android 6+).
 *
 * The receiver reschedules itself for the next day after each firing so the alarm repeats daily.
 */
class WeatherNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Use goAsync so we can launch a coroutine to hit the network
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                if (!prefs.notificationsEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val apiKey  = BuildConfig.WEATHER_API_KEY
                val remote  = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)

                // Use the real last-known location persisted by HomeViewModel.
                // Prefer GPS coords when available, fall back to city name.
                val current = if (prefs.lastLat != null && prefs.lastLon != null) {
                    remote.getCurrentWeatherByCoordinates(
                        lat   = prefs.lastLat,
                        lon   = prefs.lastLon,
                        units = prefs.units,
                        lang  = prefs.language
                    )
                } else {
                    remote.getCurrentWeatherByCityName(
                        cityName = prefs.lastCityName,
                        units    = prefs.units,
                        lang     = prefs.language
                    )
                }

                val temp = current.main.temp.toInt()
                val unit = if (prefs.units == "metric") "°C" else "°F"
                val desc = current.weather.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: ""

                postNotification(context, title = current.name, body = "$desc · $temp$unit")

                // Reschedule for the same time tomorrow
                scheduleNext(context, prefs.notificationHour, prefs.notificationMinute)
            } catch (_: Exception) {
                // Network failure — reschedule anyway so tomorrow still fires
                val prefs = runCatching {
                    UserPreferencesDataSourceImpl(context).userPreferences.first()
                }.getOrNull()
                if (prefs?.notificationsEnabled == true) {
                    scheduleNext(context, prefs.notificationHour, prefs.notificationMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Daily Weather",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily morning weather update" }
            )
        }

        // Tap notification → open app
        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ic_notification must be a simple monochrome vector in res/drawable.
        // Fall back to the built-in Android stat_notify_chat icon if it doesn't exist yet.
        val iconRes = runCatching {
            context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                .takeIf { it != 0 } ?: R.drawable.ic_dialog_info
        }.getOrDefault(R.drawable.ic_dialog_info)

        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(tapIntent)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        const val CHANNEL_ID      = "weather_daily"
        const val NOTIFICATION_ID = 1001

        // ── AlarmManager helpers ──────────────────────────────────────────────

        /**
         * Schedules the alarm at [hour]:[minute] daily.
         * Returns TRUE  → exact alarm set, notification will fire on time.
         * Returns FALSE → "Alarms & reminders" permission not granted on this device;
         *                 caller should open [openExactAlarmSettings] so the user can allow it.
         */
        fun scheduleDaily(context: Context, hour: Int, minute: Int): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                scheduleInexact(context, hour, minute)   // best-effort fallback
                return false
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMs(hour, minute),
                pendingIntent(context)
            )
            return true
        }

        /** Called by the receiver to reschedule for the same time tomorrow. */
        fun scheduleNext(context: Context, hour: Int, minute: Int) =
            scheduleDaily(context, hour, minute)

        /**
         * Opens the system "Alarms & reminders" special-permission screen for this app.
         * Call this when [scheduleDaily] returns false.
         */
        fun openExactAlarmSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data  = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }

        /** Returns true if exact alarms are currently permitted. */
        fun canScheduleExact(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }

        /**
         * Fires the notification immediately — use this to verify the whole pipeline
         * works without waiting for the scheduled time.
         */
        fun fireNow(context: Context) {
            context.sendBroadcast(Intent(context, WeatherNotificationReceiver::class.java))
        }

        /** Cancels the daily alarm. */
        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent(context))
        }

        // ── Private ───────────────────────────────────────────────────────────

        private fun scheduleInexact(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMs(hour, minute),
                pendingIntent(context)
            )
        }

        private fun nextTriggerMs(hour: Int, minute: Int): Long {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE,      minute)
                set(Calendar.SECOND,      0)
                set(Calendar.MILLISECOND, 0)
                // If the chosen time has already passed today, fire tomorrow
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WeatherNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}