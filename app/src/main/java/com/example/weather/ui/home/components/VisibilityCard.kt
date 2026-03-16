package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun VisibilityCard(
    visibilityKm: Int, modifier: Modifier = Modifier
) {
    val description = when {
        visibilityKm >= 20 -> stringResource(R.string.visibility_excellent)
        visibilityKm >= 10 -> stringResource(R.string.visibility_good)
        visibilityKm >= 4 -> stringResource(R.string.visibility_moderate)
        visibilityKm >= 1 -> stringResource(R.string.visibility_poor)
        else -> stringResource(R.string.visibility_very_poor)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(
            iconRes = R.drawable.ic_visibility, title = stringResource(R.string.card_visibility)
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$visibilityKm",
                color = WeatherColors.TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.visibility_unit),
                color = WeatherColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val startLabel =
                if (isRtl) stringResource(R.string.visibility_high) else stringResource(R.string.visibility_low)
            val endLabel =
                if (isRtl) stringResource(R.string.visibility_low) else stringResource(R.string.visibility_high)
            Text(text = startLabel, color = WeatherColors.TextSecondary, fontSize = 10.sp)
            Text(text = endLabel, color = WeatherColors.TextSecondary, fontSize = 10.sp)
        }
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (visibilityKm / 30f).coerceIn(0f, 1f))
                    .align(if (isRtl) Alignment.CenterEnd else Alignment.CenterStart)
                    .background(WeatherColors.TextPrimary)
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = description,
            color = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}