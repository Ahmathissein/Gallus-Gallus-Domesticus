package com.example.myapplication.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.YearMonth
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(onMenuClick: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    var totalAnnee by remember { mutableStateOf(0L) }
    var totalMois by remember { mutableStateOf(0L) }
    var moyenneParPouleParMois by remember { mutableStateOf(0L) }

    val currentYear = java.time.Year.now().toString()
    val currentMonth = YearMonth.now().toString() // format: yyyy-MM

    var valeursMensuelles by remember { mutableStateOf(List(12) { 0L }) }

    LaunchedEffect(Unit) {
        firestore.collection("poules")
            .document(userId)
            .collection("liste")
            .get()
            .addOnSuccessListener { snapshot ->
                val statsMensuellesTotales = mutableMapOf<String, Long>()
                var totalPoule = 0

                snapshot.documents.forEach { doc ->
                    totalPoule++
                    val statsMensuelles = doc.get("statistiquesMensuelles") as? Map<String, Long> ?: emptyMap()
                    for ((mois, valeur) in statsMensuelles) {
                        statsMensuellesTotales[mois] = (statsMensuellesTotales[mois] ?: 0) + valeur
                    }
                }

                val moisFormates = listOf(
                    "2025-01", "2025-02", "2025-03", "2025-04",
                    "2025-05", "2025-06", "2025-07", "2025-08",
                    "2025-09", "2025-10", "2025-11", "2025-12"
                )

                valeursMensuelles = moisFormates.map {
                    statsMensuellesTotales[it] ?: 0L
                }

                totalAnnee = valeursMensuelles.sum()
                totalMois = valeursMensuelles[YearMonth.now().monthValue - 1]
                moyenneParPouleParMois = if (totalPoule > 0) totalMois / totalPoule else 0
            }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6EC))
            .padding(14.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Statistiques de pontes",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037)
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatCard(value = totalAnnee, label = "Œufs produits en $currentYear")
        Spacer(modifier = Modifier.height(12.dp))

        StatCard(value = totalMois, label = "Œufs produits ce mois-ci")
        Spacer(modifier = Modifier.height(12.dp))

        StatCard(value = moyenneParPouleParMois, label = "Œufs par poule/mois en moyenne")
        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Production mensuelle",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037)
        )
        Spacer(modifier = Modifier.height(16.dp))

        BarChartWithLabels(
            values = valeursMensuelles.map { it.toFloat() },
            maxHeight = 180.dp
        )




    }
}

@Composable
fun StatCard(value: Long, label: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun BarChartWithLabels(
    values: List<Float>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 200.dp
) {
    val barColor = Color(0xCC9C5700)
    val density = LocalDensity.current
    val strokeWidth = with(density) { 1.dp.toPx() }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f // éviter division par zéro

    val months = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")

    Column {
        // Graph avec axes
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(maxHeight),
            verticalAlignment = Alignment.Bottom
        ) {
            // 🟡 Colonne des valeurs à gauche
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
                        modifier = Modifier.offset(y = (-6).dp) // ajustement vertical
                    )
                }
            }

            // 🔵 Graphique des barres
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .drawBehind {
                        val step = size.height / 4

                        // Axes
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )

                        // Lignes horizontales
                        for (i in 0..4) {
                            val y = size.height - (i * step)
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
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


        // Labels des mois
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 13.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            months.take(values.size).forEach {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037),
                )
            }
        }
    }
}
