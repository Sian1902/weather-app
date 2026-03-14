package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

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
        Text(text = label, color = WeatherColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Text(text = "$uvIndex", color = WeatherColors.TextPrimary, fontSize = 16.sp)

        Spacer(Modifier.height(12.dp))

        // UV Gradient Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(listOf(Color.Green, Color.Yellow, Color.Red, Color.Magenta)))
        ) {
            val progress = (uvIndex / 11f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color.Transparent)
            )
        }

        Spacer(Modifier.weight(1f))
        Text(
            text = description,
            color = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}