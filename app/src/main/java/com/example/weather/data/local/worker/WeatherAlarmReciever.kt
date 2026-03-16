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
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weather.AlarmActivity
import com.example.weather.BuildConfig
import com.example.weather.data.local.prefs.UserPreferencesDataSourceImpl
import com.example.weather.data.remote.WeatherRemoteDataSourceImpl
import com.example.weather.data.remote.api.RetrofitClient
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
                if (!prefs.alarmEnabled) {
                    pendingResult.finish(); return@launch
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
                val title = current.name
                val body = "$desc · $temp$unit"

                postAlarmNotification(context, title, body)
                scheduleNext(context, prefs.alarmHour, prefs.alarmMinute)
            } catch (_: Exception) {
                postAlarmNotification(context, title = "Weather Alarm", body = "")
                runCatching {
                    val prefs = UserPreferencesDataSourceImpl(context).userPreferences.first()
                    if (prefs.alarmEnabled) scheduleNext(
                        context, prefs.alarmHour, prefs.alarmMinute
                    )
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
                NotificationChannel(
                    CHANNEL_ID, "Weather Alarm", NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily weather alarm"
                    setSound(
                        alarmSound,
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    )
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 600, 400, 600)
                })
        }

        val iconRes = runCatching {
            context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                .takeIf { it != 0 } ?: R.drawable.ic_dialog_info
        }.getOrDefault(R.drawable.ic_dialog_info)

        val intent = AlarmActivity.intent(context, title, body).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenIntent = PendingIntent.getActivity(
            context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissAction = NotificationCompat.Action.Builder(
            R.drawable.ic_delete, "Dismiss", AlarmDismissReceiver.dismissPendingIntent(context)
        ).build()

        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(iconRes)
                .setContentTitle(title).setContentText(body).setAutoCancel(false).setOngoing(true)
                .addAction(dismissAction).setFullScreenIntent(fullScreenIntent, true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build()
        )
    }

    companion object {
        const val CHANNEL_ID = "weather_alarm"
        const val NOTIFICATION_ID = 1002

        fun scheduleDaily(context: Context, hour: Int, minute: Int): Boolean {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                scheduleInexact(context, hour, minute); return false
            }

            val triggerTime = nextTriggerMs(hour, minute)
            val intent = pendingIntent(context)

            val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, intent)
            am.setAlarmClock(alarmInfo, intent)

            return true
        }

        fun scheduleNext(context: Context, hour: Int, minute: Int) =
            scheduleDaily(context, hour, minute)

        fun cancel(context: Context) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
                pendingIntent(
                    context
                )
            )
        }

        private fun scheduleInexact(context: Context, hour: Int, minute: Int) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
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
            val intent = Intent(context, WeatherAlarmReceiver::class.java).apply {
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }
            return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
