package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun FeelsLikeCard(
    feelsLike     : String,
    actualTemp    : String,
    feelsLikeRaw  : Double,
    actualTempRaw : Double,
    modifier      : Modifier = Modifier
) {
    val description = when {
        feelsLikeRaw - actualTempRaw > 3  -> stringResource(R.string.feels_warmer)
        feelsLikeRaw - actualTempRaw < -3 -> stringResource(R.string.feels_colder)
        else                              -> stringResource(R.string.feels_similar)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(
            iconRes = R.drawable.ic_thermometer,
            title = stringResource(R.string.card_feels_like)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            // Use a string resource for temperature if possible,
            // otherwise ensure directionality for RTL
            text       = "$feelsLike°",
            color      = WeatherColors.TextPrimary,
            fontSize   = 36.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = stringResource(R.string.actual_temperature, actualTemp),
            color    = WeatherColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = description,
            color    = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}