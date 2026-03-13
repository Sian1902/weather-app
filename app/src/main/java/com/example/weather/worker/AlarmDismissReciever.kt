package com.example.weather.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the "Dismiss" action from the alarm notification or AlarmActivity.
 * Stops the looping sound/vibration and cancels the notification.
 */
class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmSoundManager.stop()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(WeatherAlarmReceiver.NOTIFICATION_ID)
    }

    companion object {
        const val ACTION_DISMISS = "com.example.weather.ACTION_DISMISS_ALARM"

        fun dismissPendingIntent(context: Context): android.app.PendingIntent =
            android.app.PendingIntent.getBroadcast(
                context,
                2,   // unique requestCode
                Intent(ACTION_DISMISS).setPackage(context.packageName),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
    }
}