package com.example.myapplication.screens

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
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
import com.example.myapplication.components.BarChartParPouleSection
import com.example.myapplication.components.BarChartSection
import com.example.myapplication.components.StatsPontesTable
import com.example.myapplication.poulailler.poule.Ponte
import com.example.myapplication.poulailler.poule.Poule
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

    var selectedStartDate by remember { mutableStateOf(LocalDate.now().withDayOfYear(1)) }
    var selectedEndDate by remember { mutableStateOf(LocalDate.now()) }

    val dataLoaded = remember { mutableStateOf(false) }
    val statsParPoule = remember { mutableStateMapOf<String, Long>() }
    val poulesList = remember { mutableStateListOf<Poule>() }
    val pontesData = remember { mutableStateMapOf<String, Map<LocalDate, Ponte>>() }
    var totalMauvaisOeufs by remember { mutableStateOf(0L) }
    var expandedEggMenu by remember { mutableStateOf(false) }


    // Chargement initial des stats
    LaunchedEffect(Unit) {
        val snapshot = firestore.collection("poules")
            .document(userId)
            .collection("liste")
            .get()
            .await()
        dataLoaded.value = true

        snapshot.documents.forEach { doc ->
            val poule = doc.toObject(Poule::class.java)
            val id = doc.id
            if (poule != null) {
                poulesList.add(poule)

                val pontes = doc.get("pontes") as? Map<String, Map<String, Any>> ?: emptyMap()
                val parsed = pontes.mapNotNull { (dateStr, data) ->
                    try {
                        val date = LocalDate.parse(dateStr)
                        val nb = (data["nbOeufs"] as? Long)?.toInt() ?: 0
                        val bad = (data["nbOeufsMauvais"] as? Long)?.toInt() ?: 0
                        date to Ponte(nbOeufs = nb, nbOeufsMauvais = bad)
                    } catch (e: Exception) {
                        null
                    }
                }.toMap()

                pontesData[id] = parsed
            }
        }

        var mauvais = 0L
        snapshot.documents.forEach { doc ->
            val pontes = doc.get("pontes") as? Map<String, Map<String, Any>> ?: emptyMap()
            for ((dateStr, data) in pontes) {
                try {
                    val date = LocalDate.parse(dateStr)
                    if (!date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)) {
                        mauvais += (data["nbOeufsMauvais"] as? Long) ?: 0
                    }
                } catch (_: Exception) {}
            }
        }
        totalMauvaisOeufs = mauvais

    }

    // Recalculs quand la plage de dates change
    LaunchedEffect(selectedStartDate, selectedEndDate, dataLoaded.value) {
        if (dataLoaded.value) {
            // 🔢 Total œufs produits entre selectedStartDate et selectedEndDate
            totalAnnee = pontesData.values.sumOf { pontesParDate ->
                pontesParDate.filterKeys { date ->
                    !date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)
                }.values.sumOf { (it.nbOeufs ?: 0).toLong() }

            }

            // 🔢 Moyenne par poule
            val nbPoules = poulesList.size
            moyenneParPoule = if (nbPoules > 0) totalAnnee / nbPoules else 0
            val parPoule = mutableMapOf<String, Long>()
            pontesData.forEach { (pouleId, pontesParDate) ->
                val total = pontesParDate.filterKeys { date ->
                    !date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)
                }.values.sumOf { (it.nbOeufs ?: 0).toLong() }


                val nomPoule = poulesList.find { it.id == pouleId }?.nom ?: "Inconnue"
                parPoule[nomPoule] = total
            }
            statsParPoule.clear()
            statsParPoule.putAll(parPoule)

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                selectDateRange(context, selectedStartDate, selectedEndDate) { newStart, newEnd ->
                    selectedStartDate = newStart
                    selectedEndDate = newEnd
                }
            }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Changer la période",
                    tint = Color(0xFF5D4037)
                )
            }
        }

        val optionsOeufs = listOf("Œufs produits", "Œufs mauvais", "Œufs bons")
        var selectedOeufOption by remember { mutableStateOf(optionsOeufs[0]) }
        val valeurOeuf = when (selectedOeufOption) {
            "Œufs produits" -> totalAnnee
            "Œufs mauvais" -> totalMauvaisOeufs
            "Œufs bons" -> totalAnnee - totalMauvaisOeufs
            else -> 0L
        }
        val optionsPoule = listOf("Enregistrées", "Vendues", "Disparues", "Mortes")
        var selectedPouleOption by remember { mutableStateOf(optionsPoule[0]) }

        val valeurPoules = when (selectedPouleOption) {
            "Enregistrées" -> poulesList.size.toLong()
            "Vendues" -> poulesList.count { it.estVendue }.toLong()
            "Disparues" -> poulesList.count { it.dateDisparition != null }.toLong()
            "Mortes" -> poulesList.count { it.dateDeces != null }.toLong()
            else -> 0L
        }

        var expandedPouleMenu by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedPouleMenu,
            onExpandedChange = { expandedPouleMenu = !expandedPouleMenu }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedPouleOption,
                onValueChange = {},
                label = { Text("Type de poules") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPouleMenu)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPouleMenu,
                onDismissRequest = { expandedPouleMenu = false }
            ) {
                optionsPoule.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedPouleOption = option
                            expandedPouleMenu = false
                        }
                    )
                }
            }
        }

        StatCardSimple(
            value = valeurPoules,
            label = "Nombre de poules ${selectedPouleOption.lowercase()}",
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedEggMenu,
            onExpandedChange = { expandedEggMenu = !expandedEggMenu }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedOeufOption,
                onValueChange = {},
                label = { Text("Type d'œufs") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEggMenu)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedEggMenu,
                onDismissRequest = { expandedEggMenu = false }
            ) {
                optionsOeufs.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOeufOption = option
                            expandedEggMenu = false
                        }
                    )
                }
            }
        }


        StatCardSimple(
            value = valeurOeuf,
            label = buildString {
                append("${selectedOeufOption} entre ${selectedStartDate.format(formatter)} et ${selectedEndDate.format(formatter)}")
            },
            modifier = Modifier.fillMaxWidth()
        )


        StatCardSimple(
            value = moyenneParPoule,
            label = "Moyenne œufs/poule entre ${selectedStartDate.format(formatter)}\n et ${selectedEndDate.format(formatter)}"
        )

        Spacer(modifier = Modifier.height(32.dp))

        val graphOptions = listOf("Production individuelle", "Par poule", "Mensuel", "Annuel")
        var selectedGraphOption by remember { mutableStateOf(graphOptions[0]) }
        var expandedGraphMenu by remember { mutableStateOf(false) }

        Text(
            "Graphique de production",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037),
            modifier = Modifier.padding(top = 24.dp)
        )

        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = expandedGraphMenu, onExpandedChange = { expandedGraphMenu = !expandedGraphMenu }) {
            OutlinedTextField(
                readOnly = true,
                value = selectedGraphOption,
                onValueChange = {},
                label = { Text("Choisir le graphique") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGraphMenu) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedGraphMenu,
                onDismissRequest = { expandedGraphMenu = false }
            ) {
                graphOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedGraphOption = option
                            expandedGraphMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val currentYear = Year.now().toString()
        val valeursMensuellesChart by remember(selectedStartDate, selectedEndDate, pontesData) {
            derivedStateOf {
                val moisFormates = (1..12).map { "$currentYear-${it.toString().padStart(2, '0')}" }
                val mapMois = mutableMapOf<String, Long>()

                pontesData.values.forEach { pontesParDate ->
                    pontesParDate.forEach { (date, ponte) ->
                        val mois = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        if (mois.startsWith(currentYear)) {
                            mapMois[mois] = (mapMois[mois] ?: 0) + (ponte.nbOeufs ?: 0)
                        }
                    }
                }

                moisFormates.map { mois -> mapMois[mois] ?: 0L }
            }
        }
        val mois = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")
        val decadeStart = (currentYear.toInt() / 10) * 10
        val annees = (decadeStart..decadeStart + 9).map { it.toString() }


        val valeursAnnuellesChart by remember(pontesData) {
            derivedStateOf {
                val mapAnnees = mutableMapOf<String, Long>()

                pontesData.values.forEach { pontesParDate ->
                    pontesParDate.forEach { (date, ponte) ->
                        val annee = date.year.toString()
                        mapAnnees[annee] = (mapAnnees[annee] ?: 0L) + (ponte.nbOeufs ?: 0)
                    }
                }

                // Générer une liste d'années sur 10 ans (comme decadeStart)
                val annees = (decadeStart..decadeStart + 9).map { it.toString() }
                annees.map { annee -> mapAnnees[annee] ?: 0L }
            }
        }


        when (selectedGraphOption) {
            "Mensuel" -> BarChartSection(
                title = "Production mensuelle",
                labels = mois,
                values = valeursMensuellesChart.map { it.toFloat() }
            )

            "Annuel" -> BarChartSection(
                title = "Production annuelle",
                labels = annees,
                values = valeursAnnuellesChart.map { it.toFloat() }
            )

            "Par poule" -> {
                val labels = statsParPoule.keys.toList()
                val values = statsParPoule.values.map { it.toFloat() }
                BarChartParPouleSection("Production par poule", labels, values)
            }

            "Production individuelle" -> {
                val dates = getDatesBetween(selectedStartDate, selectedEndDate)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp) // Limite pour éviter le scroll infini dans scroll
                ) {
                    StatsPontesTable(poulesList, pontesData, dates)
                }
            }



        }

        Spacer(modifier = Modifier.height(14.dp))

    }
}

@Composable
fun StatCardSimple(value: Long, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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


@RequiresApi(Build.VERSION_CODES.O)
fun selectDateRange(
    context: android.content.Context,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    Toast.makeText(context, "Sélectionnez la date de début", Toast.LENGTH_SHORT).show()
    DatePickerDialog(
        context,
        { _, yearStart, monthStart, dayStart ->
            val start = LocalDate.of(yearStart, monthStart + 1, dayStart)
            Toast.makeText(context, "Sélectionnez la date de fin", Toast.LENGTH_SHORT).show()
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


@RequiresApi(Build.VERSION_CODES.O)
fun getDatesBetween(start: LocalDate, end: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var date = start
    while (!date.isAfter(end)) {
        dates.add(date)
        date = date.plusDays(1)
    }
    return dates
}
