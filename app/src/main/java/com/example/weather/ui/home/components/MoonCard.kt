package com.example.weather.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun MoonPhaseCard(
    moonPhaseRaw: Double,
    modifier: Modifier = Modifier
) {
    // We still calculate the label so the text remains accurate
    val phaseLabel = when {
        moonPhaseRaw < 0.03 || moonPhaseRaw >= 0.97 -> stringResource(R.string.moon_new)
        moonPhaseRaw < 0.22 -> stringResource(R.string.moon_waxing_crescent)
        moonPhaseRaw < 0.28 -> stringResource(R.string.moon_first_quarter)
        moonPhaseRaw < 0.47 -> stringResource(R.string.moon_waxing_gibbous)
        moonPhaseRaw < 0.53 -> stringResource(R.string.moon_full)
        moonPhaseRaw < 0.72 -> stringResource(R.string.moon_waning_gibbous)
        moonPhaseRaw < 0.78 -> stringResource(R.string.moon_last_quarter)
        else                -> stringResource(R.string.moon_waning_crescent)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(
            iconRes = R.drawable.ic_moon,
            title = stringResource(R.string.card_moon_phase)
        )

        Spacer(Modifier.height(12.dp))

        // Single static image for all phases
        Image(
            painter = painterResource(id = R.drawable.ic_static_moon), // Your single moon asset
            contentDescription = null, // null because the Text below describes the state
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = phaseLabel,
            color = WeatherColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}