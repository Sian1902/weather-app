package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

/**
 * Visibility card.
 * Resolves its own description from strings.xml via stringResource().
 */
@Composable
fun VisibilityCard(
    visibilityKm : Int,
    modifier     : Modifier = Modifier
) {
    val description = when {
        visibilityKm >= 20 -> stringResource(R.string.visibility_excellent)
        visibilityKm >= 10 -> stringResource(R.string.visibility_good)
        visibilityKm >= 4  -> stringResource(R.string.visibility_moderate)
        visibilityKm >= 1  -> stringResource(R.string.visibility_poor)
        else               -> stringResource(R.string.visibility_very_poor)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_visibility, title = stringResource(R.string.card_visibility))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                text       = "$visibilityKm",
                color      = WeatherColors.TextPrimary,
                fontSize   = 36.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text     = stringResource(R.string.visibility_unit),
                color    = WeatherColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.visibility_low),  color = WeatherColors.TextSecondary, fontSize = 10.sp)
            Text(text = stringResource(R.string.visibility_high), color = WeatherColors.TextSecondary, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WeatherColors.TextPrimary.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(visibilityKm.coerceIn(0, 30) / 30f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WeatherColors.TextPrimary)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}