package com.example.weather

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.theme.WeatherColors
import com.example.weather.data.local.worker.AlarmDismissReceiver
import com.example.weather.data.local.worker.AlarmSoundManager

class AlarmActivity : ComponentActivity() {


    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AlarmDismissReceiver.ACTION_DISMISS) finish()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION") window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        AlarmSoundManager.start(this)

        val filter = IntentFilter(AlarmDismissReceiver.ACTION_DISMISS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dismissReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dismissReceiver, filter)
        }

        setContent {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
            val body = intent.getStringExtra(EXTRA_BODY) ?: ""

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                WeatherColors.SkyTop,
                                WeatherColors.SkyMid,
                                WeatherColors.SkyDeep,
                                WeatherColors.SkyBottom
                            )
                        )
                    ), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Time
                    val time = remember {
                        val cal = java.util.Calendar.getInstance()
                        "%02d:%02d".format(
                            cal.get(java.util.Calendar.HOUR_OF_DAY),
                            cal.get(java.util.Calendar.MINUTE)
                        )
                    }
                    Text(
                        time,
                        color = WeatherColors.TextPrimary,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light
                    )

                    if (title.isNotEmpty()) {
                        Text(
                            title,
                            color = WeatherColors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (body.isNotEmpty()) {
                        Text(
                            body, color = WeatherColors.TextSecondary, fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { dismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WeatherColors.TextPrimary.copy(alpha = 0.2f),
                            contentColor = WeatherColors.TextPrimary
                        )
                    ) {
                        Text("Dismiss", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dismissReceiver)
        AlarmSoundManager.stop()
    }

    private fun dismiss() {
        AlarmSoundManager.stop()
        finish()
    }

    companion object {
        const val EXTRA_TITLE = "alarm_title"
        const val EXTRA_BODY = "alarm_body"

        fun intent(context: Context, title: String, body: String) =
            Intent(context, AlarmActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
    }
}