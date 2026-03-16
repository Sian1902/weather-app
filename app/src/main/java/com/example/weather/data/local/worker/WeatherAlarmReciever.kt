package com.example.weather.data.local.worker

import android.R
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.weather.AlarmActivity
import com.example.weather.BuildConfig
import com.example.weather.data.local.prefs.UserPreferencesDataSourceImpl
import com.example.weather.data.remote.api.RetrofitClient
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class WeatherAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                if (!prefs.alarmEnabled) { pendingResult.finish(); return@launch }

                val apiKey = BuildConfig.WEATHER_API_KEY
                val remote = WeatherRemoteDataSourceImpl(RetrofitClient.weatherApiService, apiKey)

                val current = if (prefs.lastLat != null && prefs.lastLon != null) {
                    remote.getCurrentWeatherByCoordinates(
                        lat   = prefs.lastLat, lon = prefs.lastLon,
                        units = prefs.units,   lang = prefs.language
                    )
                } else {
                    remote.getCurrentWeatherByCityName(
                        cityName = prefs.lastCityName,
                        units    = prefs.units, lang = prefs.language
                    )
                }

                val temp  = current.main.temp.toInt()
                val unit  = if (prefs.units == "metric") "°C" else "°F"
                val desc  = current.weather.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: ""
                val title = current.name
                val body  = "$desc · $temp$unit"

                postAlarmNotification(context, title, body)
                scheduleNext(context, prefs.alarmHour, prefs.alarmMinute)
            } catch (_: Exception) {
                // Network failed — still show the alarm with no weather data
                postAlarmNotification(context, title = "Weather Alarm", body = "")
                runCatching {
                    val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                    if (prefs.alarmEnabled) scheduleNext(context, prefs.alarmHour, prefs.alarmMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postAlarmNotification(context: Context, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Weather Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Daily weather alarm"
                    setSound(alarmSound, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 600, 400, 600)
                }
            )
        }

        val iconRes = runCatching {
            context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                .takeIf { it != 0 } ?: R.drawable.ic_dialog_info
        }.getOrDefault(R.drawable.ic_dialog_info)

        // Full-screen intent — launches AlarmActivity on top of the lock screen
        val fullScreenIntent = PendingIntent.getActivity(
            context, 3,
            AlarmActivity.intent(context, title, body),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action — fires AlarmDismissReceiver without opening the app
        val dismissAction = NotificationCompat.Action.Builder(
            R.drawable.ic_delete,
            "Dismiss",
            AlarmDismissReceiver.dismissPendingIntent(context)
        ).build()

        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)           // must tap Dismiss — not swipeable
                .setOngoing(true)               // keeps it in the tray until dismissed
                .addAction(dismissAction)
                .setFullScreenIntent(fullScreenIntent, true)  // shows on lock screen
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen
                .build()
        )
    }

    companion object {
        const val CHANNEL_ID      = "weather_alarm"
        const val NOTIFICATION_ID = 1002

        fun scheduleDaily(context: Context, hour: Int, minute: Int): Boolean {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                scheduleInexact(context, hour, minute); return false
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMs(hour, minute), pendingIntent(context))
            return true
        }

        fun scheduleNext(context: Context, hour: Int, minute: Int) = scheduleDaily(context, hour, minute)

        fun cancel(context: Context) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent(context))
        }

        fun openExactAlarmSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data  = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

        fun canScheduleExact(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            return (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        }

        fun fireNow(context: Context) =
            context.sendBroadcast(Intent(context, WeatherAlarmReceiver::class.java))

        private fun scheduleInexact(context: Context, hour: Int, minute: Int) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .set(AlarmManager.RTC_WAKEUP, nextTriggerMs(hour, minute), pendingIntent(context))
        }

        private fun nextTriggerMs(hour: Int, minute: Int): Long {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE,      minute)
                set(Calendar.SECOND,      0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, WeatherAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}