package com.example.myapplication.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChartSection(
    title: String,
    labels: List<String>,
    values: List<Float>,
    maxHeight: Dp = 200.dp
) {
    val barColor = Color(0xCC9C5700)
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Graph
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val graduations = 4
                for (i in 0..graduations) {
                    val labelValue = (maxValue / graduations) * (graduations - i)
                    Text(
                        text = labelValue.toInt().toString(),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.offset(y = (-6).dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .drawBehind {
                        val step = size.height / 4
                        drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
                        drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                        for (i in 0..4) {
                            val y = size.height - (i * step)
                            drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        }
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEach { value ->
                    val barHeight = (value / maxValue) * maxHeight.value
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(
                            modifier = Modifier
                                .height(barHeight.dp)
                                .width(16.dp)
                                .background(barColor, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 13.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.take(values.size).forEach {
                Text(text = it, fontSize = 12.sp, color = Color(0xFF5D4037))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

