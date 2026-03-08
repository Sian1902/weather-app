package com.example.weather.ui.home

import com.example.weather.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*

private val CardBg       = Color(0xFF1E3A5F).copy(alpha = 0.65f)
private val CardBgDark   = Color(0xFF152D4A).copy(alpha = 0.70f)
private val White        = Color.White
private val WhiteDim     = Color.White.copy(alpha = 0.70f)
private val GoldLine     = Color(0xFFFFC107)
private val CyanLine     = Color(0xFF4DD0E1)
private val UvGreen      = Color(0xFF8BC34A)
private val UvYellow     = Color(0xFFFFEB3B)
private val UvOrange     = Color(0xFFFF9800)
private val UvRed        = Color(0xFFF44336)
private val UvPurple     = Color(0xFF9C27B0)
private val DividerColor = Color.White.copy(alpha = 0.15f)

data class HourlyItem(val label: String, val iconRes: Int, val temp: String)
data class DailyItem(val day: String, val date: String, val iconRes: Int, val high: String, val low: String)

@Composable
fun HomeScreen(
    cityName: String = "Cairo",
    currentTemp: String = "14",
    highTemp: String = "23",
    lowTemp: String = "13",
    weatherDescription: String = "Cloudy",
    feelsLike: String = "13",
    actualTemp: String = "14",
    feelsLikeDescription: String = "Feels similar to actual temperature",
    uvIndex: Int = 1,
    uvLabel: String = "Minimal",
    uvDescription: String = "Almost no risk of sunburn",
    windSpeedBft: Int = 2,
    windDeg: Int = 315,
    windDescription: String = "NW wind, gentle breeze on the face",
    humidity: Int = 76,
    humidityDescription: String = "Fairly humid, dew is likely to form",
    visibilityKm: Int = 30,
    visibilityDescription: String = "Excellent visibility",
    pressure: Int = 1021,
    pressureDescription: String = "Slightly high pressure",
    sunriseTime: String = "6:14",
    sunsetTime: String = "17:58",
    hourlyItems: List<HourlyItem> = sampleHourly(),
    dailyItems: List<DailyItem> = sampleDaily(),
    onMenuClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A90C4),
                        Color(0xFF2E6EA6),
                        Color(0xFF1C4F80),
                        Color(0xFF6B9FBF)
                    )
                )
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(0.3f, 0.2f),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            TopBar(
                cityName = cityName,
                subtitle = null,
                onMenuClick = onMenuClick,
                onMoreClick = onMoreClick
            )

            HeroSection(
                currentTemp = currentTemp,
                highTemp = highTemp,
                lowTemp = lowTemp
            )

            Spacer(modifier = Modifier.height(24.dp))

            HourlyWeatherCard(items = hourlyItems)

            Spacer(modifier = Modifier.height(12.dp))

            ForecastCard(items = dailyItems)

            Spacer(modifier = Modifier.height(12.dp))


            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UvIndexCard(
                        uvIndex = uvIndex,
                        label = uvLabel,
                        description = uvDescription,
                        modifier = Modifier.weight(1f)
                    )
                    FeelsLikeCard(
                        feelsLike = feelsLike,
                        actualTemp = actualTemp,
                        description = feelsLikeDescription,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindCard(
                        speedBft = windSpeedBft,
                        deg = windDeg,
                        description = windDescription,
                        modifier = Modifier.weight(1f)
                    )
                    SunCard(
                        sunriseTime = sunriseTime,
                        sunsetTime = sunsetTime,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HumidityCard(
                        humidity = humidity,
                        description = humidityDescription,
                        modifier = Modifier.weight(1f)
                    )
                    VisibilityCard(
                        visibilityKm = visibilityKm,
                        description = visibilityDescription,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PressureCard(
                        pressure = pressure,
                        description = pressureDescription,
                        modifier = Modifier.weight(1f)
                    )
                    MoonPhaseCard(
                        phase = "Waning gibbous",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TopBar(
    cityName: String,
    subtitle: String?,
    onMenuClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_add),
                contentDescription = "Menu",
                tint = White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cityName,
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(text = subtitle, color = WhiteDim, fontSize = 13.sp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = WhiteDim,
                        modifier = Modifier.size(14.dp)
                    )
                    repeat(2) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == 0) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (i == 0) White else WhiteDim)
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = White
            )
        }
    }
}

@Composable
fun HeroSection(currentTemp: String, highTemp: String, lowTemp: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$currentTemp°",
            color = White,
            fontSize = 96.sp,
            fontWeight = FontWeight.Thin,
            lineHeight = 96.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$highTemp°/$lowTemp°",
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun HourlyWeatherCard(items: List<HourlyItem>) {
    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bell),
                    contentDescription = null,
                    tint = WhiteDim,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hourly weather", color = WhiteDim, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = DividerColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(items) { item ->
                    HourlyItemView(item)
                }
            }
        }
    }
}

@Composable
fun HourlyItemView(item: HourlyItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = item.label,
            color = if (item.label == "Now") White else WhiteDim,
            fontSize = 13.sp,
            fontWeight = if (item.label == "Now") FontWeight.Bold else FontWeight.Normal
        )
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
        Text(text = item.temp, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ForecastCard(items: List<DailyItem>) {
    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = WhiteDim,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Weather forecast", color = WhiteDim, fontSize = 13.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list_view),
                        contentDescription = "List",
                        tint = WhiteDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chart_view),
                        contentDescription = "Chart",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    DailyItemView(item = item, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            SparkLine(
                values = items.map { it.high.removeSuffix("°").toFloat() },
                color = GoldLine
            )

            Spacer(modifier = Modifier.height(4.dp))
            SparkLine(
                values = items.map { it.low.removeSuffix("°").toFloat() },
                color = CyanLine
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    Text(
                        text = item.low,
                        color = WhiteDim,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Extended forecast", color = White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DailyItemView(item: DailyItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.day,
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(text = item.date, color = WhiteDim, fontSize = 11.sp)
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )
        Text(text = item.high, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SparkLine(values: List<Float>, color: Color) {
    if (values.size < 2) return
    val min = values.min()
    val max = values.max()
    val range = (max - min).takeIf { it > 0 } ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val w = size.width
        val h = size.height
        val step = w / (values.size - 1)

        val pts = values.mapIndexed { i, v ->
            Offset(x = i * step, y = h - ((v - min) / range) * h * 0.8f - h * 0.1f)
        }

        // Line
        for (i in 0 until pts.size - 1) {
            drawLine(
                color = color,
                start = pts[i],
                end = pts[i + 1],
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        pts.forEach { pt ->
            drawCircle(color = color, radius = 4.dp.toPx(), center = pt)
            drawCircle(
                color = Color(0xFF1E3A5F),
                radius = 2.5.dp.toPx(),
                center = pt
            )
        }
    }
}


@Composable
fun UvIndexCard(uvIndex: Int, label: String, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_uv, title = "UV index")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "$uvIndex", color = White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        UvBar(uvIndex = uvIndex)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun UvBar(uvIndex: Int) {
    val uvColors = listOf(UvGreen, UvGreen, UvYellow, UvYellow, UvOrange, UvOrange, UvRed, UvRed, UvPurple, UvPurple, UvPurple)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(UvGreen, UvYellow, UvOrange, UvRed, UvPurple)
                )
            )
    ) {
        val fraction = (uvIndex.coerceIn(0, 11) / 11f)
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
                    .background(White)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun FeelsLikeCard(feelsLike: String, actualTemp: String, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_thermometer, title = "Feels like")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$feelsLike°", color = White, fontSize = 36.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Actual temperature: $actualTemp°", color = WhiteDim, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun WindCard(speedBft: Int, deg: Int, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(iconRes = R.drawable.ic_wind, title = "Wind force")
        Spacer(modifier = Modifier.height(8.dp))
        WindCompass(speedBft = speedBft, deg = deg, modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun WindCompass(speedBft: Int, deg: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = size.minDimension / 2f

        for (i in 0 until 72) {
            val angle = Math.toRadians(i * 5.0)
            val tickLen = if (i % 9 == 0) 10.dp.toPx() else 5.dp.toPx()
            val outerR = r - 2.dp.toPx()
            val innerR = outerR - tickLen
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(cx + outerR * Math.cos(angle).toFloat(), cy + outerR * Math.sin(angle).toFloat()),
                end   = Offset(cx + innerR * Math.cos(angle).toFloat(), cy + innerR * Math.sin(angle).toFloat()),
                strokeWidth = 1.dp.toPx()
            )
        }

        drawCircle(color = Color(0xFF1E3A5F).copy(alpha = 0.6f), radius = r * 0.55f, center = Offset(cx, cy))
        drawCircle(color = Color.White.copy(alpha = 0.2f), radius = r * 0.55f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))

        val arrowAngle = Math.toRadians(deg.toDouble() - 90)
        val arrowLen = r * 0.45f
        val arrowEnd = Offset(
            cx + arrowLen * Math.cos(arrowAngle).toFloat(),
            cy + arrowLen * Math.sin(arrowAngle).toFloat()
        )
        drawLine(
            color = Color.White,
            start = Offset(cx, cy),
            end = arrowEnd,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        val headAngle1 = arrowAngle + Math.toRadians(150.0)
        val headAngle2 = arrowAngle - Math.toRadians(150.0)
        val headLen = 12.dp.toPx()
        drawLine(color = Color.White, start = arrowEnd,
            end = Offset(arrowEnd.x + headLen * Math.cos(headAngle1).toFloat(), arrowEnd.y + headLen * Math.sin(headAngle1).toFloat()),
            strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = Color.White, start = arrowEnd,
            end = Offset(arrowEnd.x + headLen * Math.cos(headAngle2).toFloat(), arrowEnd.y + headLen * Math.sin(headAngle2).toFloat()),
            strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, cy))
    }
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$speedBft", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Bft", color = WhiteDim, fontSize = 11.sp)
        }
    }
}

@Composable
fun SunCard(sunriseTime: String, sunsetTime: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(iconRes = R.drawable.ic_sunset, title = "Sunset")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = sunsetTime, color = White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(8.dp))
        SunArc(modifier = Modifier.fillMaxWidth().height(60.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Sunrise: $sunriseTime", color = WhiteDim, fontSize = 12.sp)
    }
}

@Composable
fun SunArc(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h)
        }
        drawPath(path, color = Color.White.copy(alpha = 0.3f), style = Stroke(1.5.dp.toPx()))
        val t = 0.6f
        val sunX = w * t
        val sunY = 4 * h * t * (1 - t) * (-1f) + h  // rough bezier approx
        drawCircle(color = Color(0xFFFFC107), radius = 7.dp.toPx(), center = Offset(sunX, sunY.coerceIn(0f, h)))
    }
}

@Composable
fun HumidityCard(humidity: Int, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_humidity, title = "Humidity")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$humidity%", color = White, fontSize = 36.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun VisibilityCard(visibilityKm: Int, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_visibility, title = "Visibility")
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "$visibilityKm", color = White, fontSize = 36.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "km", color = WhiteDim, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Low", color = WhiteDim, fontSize = 10.sp)
            Text(text = "High", color = WhiteDim, fontSize = 10.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            val fraction = (visibilityKm.coerceIn(0, 30) / 30f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(White)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun PressureCard(pressure: Int, description: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_pressure, title = "Pressure")
        Spacer(modifier = Modifier.height(8.dp))
        PressureGauge(
            pressure = pressure,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WhiteDim, fontSize = 11.sp)
    }
}

@Composable
fun PressureGauge(pressure: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f - 4.dp.toPx()
            for (i in 0 until 60) {
                val angle = Math.toRadians(i * 6.0 - 150.0)
                val tickLen = if (i % 5 == 0) 8.dp.toPx() else 4.dp.toPx()
                val outerR = r
                val innerR = r - tickLen
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(cx + outerR * Math.cos(angle).toFloat(), cy + outerR * Math.sin(angle).toFloat()),
                    end   = Offset(cx + innerR * Math.cos(angle).toFloat(), cy + innerR * Math.sin(angle).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            val normalized = ((pressure - 950f) / 100f).coerceIn(0f, 1f)
            val needleAngle = Math.toRadians((-120 + normalized * 240).toDouble())
            val needleLen = r * 0.7f
            drawLine(
                color = Color.White,
                start = Offset(cx, cy),
                end = Offset(cx + needleLen * Math.cos(needleAngle).toFloat(), cy + needleLen * Math.sin(needleAngle).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "$pressure", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "hPa", color = WhiteDim, fontSize = 10.sp)
        }
    }
}

@Composable
fun MoonPhaseCard(phase: String, modifier: Modifier = Modifier) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_moon, title = "Moon phase")
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f
            drawCircle(color = Color(0xFFCCCCCC), radius = r, center = Offset(cx, cy))
            drawCircle(
                color = Color(0xFF1E3A5F).copy(alpha = 0.7f),
                radius = r,
                center = Offset(cx - r * 0.35f, cy)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = phase, color = WhiteDim, fontSize = 12.sp)
    }
}


@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
    ) {
        content()
    }
}

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    minHeight: Dp = 150.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBgDark)
            .padding(14.dp)
            .defaultMinSize(minHeight = minHeight),
        content = content
    )
}

@Composable
fun CardHeader(iconRes: Int, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = WhiteDim,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = title, color = WhiteDim, fontSize = 12.sp)
    }
}


fun sampleHourly() = listOf(
    HourlyItem("Now",   R.drawable.ic_weather_partly_cloudy, "14°"),
    HourlyItem("10:00", R.drawable.ic_weather_sunny,          "17°"),
    HourlyItem("11:00", R.drawable.ic_weather_sunny,          "19°"),
    HourlyItem("12:00", R.drawable.ic_weather_sunny,          "20°"),
    HourlyItem("13:00", R.drawable.ic_weather_sunny,          "21°"),
    HourlyItem("14:00", R.drawable.ic_weather_sunny,          "22°"),
    HourlyItem("15:00", R.drawable.ic_weather_partly_cloudy,  "21°"),
)

fun sampleDaily() = listOf(
    DailyItem("Today", "3/7",  R.drawable.ic_weather_partly_cloudy, "23°", "13°"),
    DailyItem("Sun",   "3/8",  R.drawable.ic_weather_partly_cloudy, "21°", "13°"),
    DailyItem("Mon",   "3/9",  R.drawable.ic_weather_partly_cloudy, "20°", "12°"),
    DailyItem("Tue",   "3/10", R.drawable.ic_weather_partly_cloudy, "22°", "12°"),
    DailyItem("Wed",   "3/11", R.drawable.ic_weather_partly_cloudy, "24°", "13°"),
)

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}