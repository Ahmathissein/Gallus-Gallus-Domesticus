package com.example.myapplication.screens

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.BarChartSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.*
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(onMenuClick: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var totalAnnee by remember { mutableStateOf(0L) }
    var moyenneParPoule by remember { mutableStateOf(0L) }

    var valeursMensuelles by remember { mutableStateOf(List(12) { 0L }) }
    var valeursAnnuelles by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    var startDateTotal by remember { mutableStateOf(LocalDate.now().withDayOfYear(1)) }
    var endDateTotal by remember { mutableStateOf(LocalDate.now()) }

    var startDateMoyenne by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var endDateMoyenne by remember { mutableStateOf(LocalDate.now()) }

    val statsMensuellesTotales = remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    val dataLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val snapshot = firestore.collection("poules")
            .document(userId)
            .collection("liste")
            .get()
            .await()

        val totales = mutableMapOf<String, Long>()
        val annuelles = mutableMapOf<String, Long>()
        var totalPoule = 0

        snapshot.documents.forEach { doc ->
            totalPoule++
            val statsMensuelles = doc.get("statistiquesMensuelles") as? Map<String, Long> ?: emptyMap()
            for ((mois, valeur) in statsMensuelles) {
                totales[mois] = (totales[mois] ?: 0) + valeur
            }

            val statsAnnuelles = doc.get("statistiquesAnnuelles") as? Map<String, Long> ?: emptyMap()
            for ((annee, valeur) in statsAnnuelles) {
                annuelles[annee] = (annuelles[annee] ?: 0) + valeur
            }
        }

        statsMensuellesTotales.value = totales
        valeursAnnuelles = annuelles.toSortedMap()
        dataLoaded.value = true // ✅ On indique que les données sont bien là
    }


    LaunchedEffect(startDateTotal, endDateTotal, dataLoaded.value) {
        if (dataLoaded.value) {
            val moisDebut = YearMonth.from(startDateTotal)
            val moisFin = YearMonth.from(endDateTotal)
            val stats = statsMensuellesTotales.value.filterKeys {
                val ym = YearMonth.parse(it)
                !ym.isBefore(moisDebut) && !ym.isAfter(moisFin)
            }
            totalAnnee = stats.values.sum()
        }
    }

    LaunchedEffect(startDateMoyenne, endDateMoyenne, dataLoaded.value) {
        if (dataLoaded.value) {
            val moisDebut = YearMonth.from(startDateMoyenne)
            val moisFin = YearMonth.from(endDateMoyenne)
            val stats = statsMensuellesTotales.value.filterKeys {
                val ym = YearMonth.parse(it)
                !ym.isBefore(moisDebut) && !ym.isAfter(moisFin)
            }

            val snapshot = firestore.collection("poules")
                .document(userId)
                .collection("liste")
                .get()
                .await()
            val nbPoules = snapshot.size()
            val total = stats.values.sum()
            moyenneParPoule = if (nbPoules > 0) total / nbPoules else 0
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

        // Carte 1 : Total œufs
        println("TTTTTTTTTTTTTTTTTTTTTTTTTTotal annee ${totalAnnee}")
        StatCardWithDatePicker(
            value = totalAnnee,
            label = "Œufs produits entre ${startDateTotal.format(formatter)}\n et ${endDateTotal.format(formatter)}",
            onDateClick = {
                selectDateRange(
                    context = context,
                    initialStart = startDateTotal,
                    initialEnd = endDateTotal
                ) { newStart, newEnd ->
                    startDateTotal = newStart
                    endDateTotal = newEnd
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carte 2 : Moyenne œufs/poule
        StatCardWithDatePicker(
            value = moyenneParPoule,
            label = "Moyenne œufs/poule entre ${startDateMoyenne.format(formatter)}\n et ${endDateMoyenne.format(formatter)}",
            onDateClick = {
                selectDateRange(
                    context = context,
                    initialStart = startDateMoyenne,
                    initialEnd = endDateMoyenne
                ) { newStart, newEnd ->
                    startDateMoyenne = newStart
                    endDateMoyenne = newEnd
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
        val currentYear = Year.now().toString()

        val valeursMensuellesChart by remember {
            derivedStateOf {
                val moisFormates = listOf(
                    "$currentYear-01", "$currentYear-02", "$currentYear-03", "$currentYear-04",
                    "$currentYear-05", "$currentYear-06", "$currentYear-07", "$currentYear-08",
                    "$currentYear-09", "$currentYear-10", "$currentYear-11", "$currentYear-12"
                )
                moisFormates.map { mois -> statsMensuellesTotales.value[mois] ?: 0L }
            }
        }


        val mois = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")
        BarChartSection(
            title = "Production mensuelle",
            labels = mois,
            values = valeursMensuellesChart.map { it.toFloat() }
        )

        val decadeStart = (currentYear.toInt() / 10) * 10
        val annees = (decadeStart..decadeStart + 9).map { it.toString() }

        BarChartSection(
            title = "Production annuelle",
            labels = annees,
            values = annees.map { annee -> valeursAnnuelles[annee] ?: 0L }.map { it.toFloat() }
        )
    }
}


// Carte avec icône calendrier à droite
@Composable
fun StatCardWithDatePicker(value: Long, label: String, onDateClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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

            IconButton(onClick = onDateClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Sélectionner la date",
                    tint = Color(0xFF5D4037)
                )
            }
        }
    }
}

// Utilitaire : calcul du nombre de mois entre deux YearMonth inclusivement
@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.untilInclusive(other: YearMonth): Int {
    return Period.between(this.atDay(1), other.atEndOfMonth()).toTotalMonths().toInt().coerceAtLeast(1)
}


@RequiresApi(Build.VERSION_CODES.O)
fun selectDateRange(
    context: android.content.Context,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    // Affiche un message "date de début"
    android.widget.Toast.makeText(context, "Sélectionnez la date de début", android.widget.Toast.LENGTH_SHORT).show()

    DatePickerDialog(
        context,
        { _, yearStart, monthStart, dayStart ->
            val start = LocalDate.of(yearStart, monthStart + 1, dayStart)

            // Affiche un message "date de fin"
            android.widget.Toast.makeText(context, "Sélectionnez la date de fin", android.widget.Toast.LENGTH_SHORT).show()

            DatePickerDialog(
                context,
                { _, yearEnd, monthEnd, dayEnd ->
                    val end = LocalDate.of(yearEnd, monthEnd + 1, dayEnd)
                    onRangeSelected(start, end)
                },
                initialEnd.year, initialEnd.monthValue - 1, initialEnd.dayOfMonth
            ).show()

        },
        initialStart.year, initialStart.monthValue - 1, initialStart.dayOfMonth
    ).show()
}


