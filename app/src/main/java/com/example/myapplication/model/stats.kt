package com.example.myapplication.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.myapplication.components.BarChartSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.YearMonth
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(onMenuClick: () -> Unit) {
    //val scrollState = rememberScrollState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    var totalAnnee by remember { mutableStateOf(0L) }
    var totalMois by remember { mutableStateOf(0L) }
    var moyenneParPouleParMois by remember { mutableStateOf(0L) }

    val currentYear = java.time.Year.now().toString()
    val currentMonth = YearMonth.now().toString() // format: yyyy-MM

    var valeursMensuelles by remember { mutableStateOf(List(12) { 0L }) }
    var valeursAnnuelles by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }



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

                val statsAnnuellesTotales = mutableMapOf<String, Long>()

                snapshot.documents.forEach { doc ->
                    totalPoule++
                    val statsMensuelles = doc.get("statistiquesMensuelles") as? Map<String, Long> ?: emptyMap()
                    for ((mois, valeur) in statsMensuelles) {
                        statsMensuellesTotales[mois] = (statsMensuellesTotales[mois] ?: 0) + valeur
                    }

                    val statsAnnuelles = doc.get("statistiquesAnnuelles") as? Map<String, Long> ?: emptyMap()
                    for ((annee, valeur) in statsAnnuelles) {
                        statsAnnuellesTotales[annee] = (statsAnnuellesTotales[annee] ?: 0) + valeur
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


                valeursMensuelles = moisFormates.map { statsMensuellesTotales[it] ?: 0L }
                val currentYearTotal = valeursMensuelles.sum()
                statsAnnuellesTotales[currentYear] = currentYearTotal

                valeursAnnuelles = statsAnnuellesTotales.toSortedMap()

            }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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


        val mois = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")
        BarChartSection(
            title = "Production mensuelle",
            labels = mois,
            values = valeursMensuelles.map { it.toFloat() }        )


        val decadeStart = (currentYear.toInt() / 10) * 10
        val annees = (decadeStart..decadeStart + 9).map { it.toString() }

        BarChartSection(
            title = "Production annuelle",
            labels = annees,
            values = annees.map { annee ->
                valeursAnnuelles[annee] ?: 0L
            }.map { it.toFloat() }
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


