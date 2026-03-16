package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun SparkLine(values: List<Float>, color: Color) {
    if (values.size < 2) return

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val orderedValues = if (isRtl) values.reversed() else values

    val min = orderedValues.minOrNull() ?: 0f
    val max = orderedValues.maxOrNull() ?: 1f
    val range = (max - min).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val w = size.width
        val h = size.height
        val step = w / (orderedValues.size - 1)


        val points = orderedValues.mapIndexed { i, v ->
            Offset(
                x = i * step, y = h - ((v - min) / range) * (h * 0.8f) - (h * 0.1f)
            )
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}