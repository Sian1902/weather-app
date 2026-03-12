package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

/**
 * Humidity card.
 * Resolves its own description from strings.xml via stringResource().
 */
@Composable
fun HumidityCard(
    humidity : Int,
    modifier : Modifier = Modifier
) {
    val description = when {
        humidity < 30 -> stringResource(R.string.humidity_very_dry)
        humidity < 50 -> stringResource(R.string.humidity_comfortable)
        humidity < 60 -> stringResource(R.string.humidity_slightly)
        humidity < 75 -> stringResource(R.string.humidity_fairly)
        humidity < 90 -> stringResource(R.string.humidity_high)
        else          -> stringResource(R.string.humidity_very_high)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_humidity, title = stringResource(R.string.card_humidity))
        Spacer(Modifier.height(8.dp))
        Text(
            text       = "$humidity%",
            color      = WeatherColors.TextPrimary,
            fontSize   = 36.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(Modifier.weight(1f))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}