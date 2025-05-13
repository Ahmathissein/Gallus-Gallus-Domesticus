package com.example.myapplication.components

import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.times
import kotlin.math.pow

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.poulailler.poule.Ponte
import com.example.myapplication.poulailler.poule.Poule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow


@Composable
fun BarChartSection(
    title: String,
    labels: List<String>,
    values: List<Float>,
    maxHeight: Dp = 200.dp
) {
    val barColor = Color(0xCC9C5700)
    val rawMax = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

// 🔢 On arrondit le max à une valeur "ronde"
    val exponent = kotlin.math.floor(kotlin.math.log10(rawMax.toDouble())).toInt()
    val base = 10.0.pow(exponent).toFloat()
    val roundedMax = ((rawMax / base).toInt() + 1) * base

    val maxValue = roundedMax

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
                    if (value > 0f) {
                        val barHeight = (value / maxValue) * maxHeight.value
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = value.toInt().toString(),
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Spacer(
                                modifier = Modifier
                                    .height(barHeight.dp)
                                    .width(16.dp)
                                    .background(barColor, RoundedCornerShape(4.dp))
                            )
                        }
                    } else {
                        // espace vide pour garder l'espacement homogène
                        Spacer(modifier = Modifier.width(16.dp))
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



@Composable
fun BarChartParPouleSection(
    title: String,
    labels: List<String>,
    values: List<Float>,
    barMaxWidth: Dp = 240.dp
) {
    val barColor = Color(0xCC9C5700)
    val rawMax = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    val exponent = kotlin.math.floor(kotlin.math.log10(rawMax.toDouble())).toInt()
    val base = 10.0.pow(exponent).toFloat()
    val roundedMax = listOf(10f, 25f, 50f, 100f, 250f, 500f, 1000f, 2500f)
        .first { it >= rawMax }

    val scrollState = rememberScrollState()

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val y = size.height // 20.dp = hauteur de la ligne des valeurs
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(80.dp.toPx(), y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .drawBehind {
                            drawLine(
                                color = Color.DarkGray,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height + 4.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    labels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .fillMaxWidth()
                                .padding(start = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    values.forEach { value ->
                        val ratio = value / roundedMax
                        val barWidth = barMaxWidth * ratio

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(barWidth)
                                    .background(barColor, RoundedCornerShape(4.dp))
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = value.toInt().toString(),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // Graduations en bas sans espace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(start = 94.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..4) {
                val labelValue = (roundedMax / 4) * i
                Text(
                    text = labelValue.toInt().toString(),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsPontesTable(
    poules: List<Poule>,
    pontesData: Map<String, Map<LocalDate, Ponte>>,
    dates: List<LocalDate>

) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.FRENCH)
    val vertState = rememberScrollState()
    val horState  = rememberScrollState()

    Column {
        Row {
            // première cellule vide
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text("Poule", fontWeight = FontWeight.Bold)
            }
            // dates
            Row(modifier = Modifier.horizontalScroll(horState)) {
                dates.forEach { date ->
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(date.format(dateFormatter), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Divider()

        // ── Corps du tableau ─────────────────────────────────────────────────────
        Row {
            // colonne des noms + lignes “Total”
            Column(modifier = Modifier.verticalScroll(vertState)) {
                poules.forEach { poule ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(poule.nom, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                // Total œufs
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Total œufs", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
                // Total mauvais
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Total mauvais", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
            }

            // cellule des valeurs
            Row(
                modifier = Modifier
                    .horizontalScroll(horState)
                    .verticalScroll(vertState)
            ) {
                dates.forEach { date ->
                    Column {
                        // ligne par poule
                        poules.forEach { poule ->
                            val p = pontesData[poule.id]?.get(date)
                            val total = p?.nbOeufs ?: 0
                            val mauvais = p?.nbOeufsMauvais ?: 0
                            val bons = total - mauvais
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (total > 0) {
                                    Text("$bons / $total", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("-", fontWeight = FontWeight.Bold)
                                }
                            }

                        }
                        // total œufs
                        val totalEggs = poules.sumOf { pontesData[it.id]?.get(date)?.nbOeufs ?: 0 }
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(totalEggs.toString(), fontWeight = FontWeight.Bold)
                        }
                        // total mauvais
                        val totalBad = poules.sumOf { pontesData[it.id]?.get(date)?.nbOeufsMauvais ?: 0 }
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(totalBad.toString(), color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}






