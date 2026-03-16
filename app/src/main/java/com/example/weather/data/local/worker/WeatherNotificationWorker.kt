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
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.remote.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class WeatherNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                if (!prefs.notificationsEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val apiKey = BuildConfig.WEATHER_API_KEY
                val remote = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)

                val current = if (prefs.lastLat != null && prefs.lastLon != null) {
                    remote.getCurrentWeatherByCoordinates(
                        lat = prefs.lastLat,
                        lon = prefs.lastLon,
                        units = prefs.units,
                        lang = prefs.language
                    )
                } else {
                    remote.getCurrentWeatherByCityName(
                        cityName = prefs.lastCityName, units = prefs.units, lang = prefs.language
                    )
                }

                val temp = current.main.temp.toInt()
                val unit = if (prefs.units == "metric") "°C" else "°F"
                val desc =
                    current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                        ?: ""

                postNotification(context, title = current.name, body = "$desc · $temp$unit")

                scheduleNext(context, prefs.notificationHour, prefs.notificationMinute)
            } catch (_: Exception) {
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
                    CHANNEL_ID, "Daily Weather", NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Daily morning weather update" })
        }

        val tapIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = runCatching {
            context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                .takeIf { it != 0 } ?: R.drawable.ic_dialog_info
        }.getOrDefault(R.drawable.ic_dialog_info)

        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(iconRes)
                .setContentTitle(title).setContentText(body).setContentIntent(tapIntent)
                .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        )
    }

    companion object {
        const val CHANNEL_ID = "weather_daily"
        const val NOTIFICATION_ID = 1001

        fun scheduleDaily(context: Context, hour: Int, minute: Int): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                scheduleInexact(context, hour, minute)
                return false
            }

            val triggerTime = nextTriggerMs(hour, minute)
            val intent = pendingIntent(context)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, intent
            )
            return true
        }

        fun scheduleNext(context: Context, hour: Int, minute: Int) =
            scheduleDaily(context, hour, minute)

        fun openExactAlarmSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
            }
        }

        fun canScheduleExact(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }


        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent(context))
        }

        private fun scheduleInexact(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.RTC_WAKEUP, nextTriggerMs(hour, minute), pendingIntent(context)
            )
        }

        private fun nextTriggerMs(hour: Int, minute: Int): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        private fun pendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WeatherNotificationReceiver::class.java).apply {
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}