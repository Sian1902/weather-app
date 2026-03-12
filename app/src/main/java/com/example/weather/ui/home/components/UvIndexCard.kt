package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

/**
 * UV index card.
 * Receives only the raw integer — resolves its own label and description
 * from strings.xml via stringResource() so Arabic/English switch is instant.
 */
@Composable
fun UvIndexCard(
    uvIndex  : Int,
    modifier : Modifier = Modifier
) {
    val label = when {
        uvIndex <= 2  -> stringResource(R.string.uv_minimal)
        uvIndex <= 5  -> stringResource(R.string.uv_moderate)
        uvIndex <= 7  -> stringResource(R.string.uv_high)
        uvIndex <= 10 -> stringResource(R.string.uv_very_high)
        else          -> stringResource(R.string.uv_extreme)
    }
    val description = when {
        uvIndex <= 2  -> stringResource(R.string.uv_desc_minimal)
        uvIndex <= 5  -> stringResource(R.string.uv_desc_moderate)
        uvIndex <= 7  -> stringResource(R.string.uv_desc_high)
        uvIndex <= 10 -> stringResource(R.string.uv_desc_very_high)
        else          -> stringResource(R.string.uv_desc_extreme)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_uv, title = stringResource(R.string.card_uv_index))
        Spacer(Modifier.height(8.dp))
        Text(text = label,      color = WeatherColors.TextPrimary,   fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "$uvIndex", color = WeatherColors.TextPrimary,   fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        UvBar(uvIndex)
        Spacer(Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun UvBar(uvIndex: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        WeatherColors.UvGreen, WeatherColors.UvYellow,
                        WeatherColors.UvOrange, WeatherColors.UvRed, WeatherColors.UvPurple
                    )
                )
            )
    ) {
        val fraction = uvIndex.coerceIn(0, 11) / 11f
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .wrapContentWidth(Alignment.End)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(WeatherColors.TextPrimary)
                    .align(Alignment.Center)
            )
        }
    }
}