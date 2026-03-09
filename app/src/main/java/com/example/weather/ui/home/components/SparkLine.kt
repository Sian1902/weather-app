package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp


@Composable
fun SparkLine(values: List<Float>, color: Color) {
    if (values.size < 2) return

    val min   = values.min()
    val max   = values.max()
    val range = (max - min).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val w    = size.width
        val h    = size.height
        val step = w / (values.size - 1)

        val points = values.mapIndexed { i, v ->
            Offset(
                x = i * step,
                y = h - ((v - min) / range) * h * 0.8f - h * 0.1f
            )
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color       = color,
                start       = points[i],
                end         = points[i + 1],
                strokeWidth = 2.5.dp.toPx(),
                cap         = StrokeCap.Round
            )
        }

        points.forEach { pt ->
            drawCircle(color = color,                      radius = 4.dp.toPx(),   center = pt)
            drawCircle(color = Color(0xFF1E3A5F),          radius = 2.5.dp.toPx(), center = pt)
        }
    }
}