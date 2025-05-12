package com.example.myapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RentabiliteScreen() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val defaultStart = LocalDate.of(today.year, 1, 1)

    var startDate by remember { mutableStateOf(defaultStart) }
    var endDate by remember { mutableStateOf(today) }
    var dataLoaded by remember { mutableStateOf(false) }

    val revenusParMois = remember { mutableStateMapOf<String, Double>() }
    val depensesParMois = remember { mutableStateMapOf<String, Double>() }

    var nbOeufs by remember { mutableStateOf(0) }
    var revenuOeufs by remember { mutableStateOf(0.0) }
    var nbPoulesVendues by remember { mutableStateOf(0) }
    var revenuPoules by remember { mutableStateOf(0.0) }
    var depensesParType by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var coutTotal by remember { mutableStateOf(0.0) }


    // 🔁 Récupération des données
    LaunchedEffect(startDate, endDate, dataLoaded) {
        if (!dataLoaded) {
            revenusParMois.clear()
            depensesParMois.clear()

            val poulesSnap = firestore.collection("poules").document(userId).collection("liste").get().await()
            val ventesSnap = firestore.collection("ventes").document(userId).collection("liste").get().await()
            val evenementsSnap = firestore.collection("evenements_poulailler").document(userId).collection("liste").get().await()

            // 🔹 Filtrage des ventes dans la période
            val ventesFiltrees = ventesSnap.documents.filter {
                val d = it.getString("date")?.let { dateStr -> LocalDate.parse(dateStr) } ?: return@filter false
                d in startDate..endDate
            }

            // 🔹 Ventes d'œufs
            val ventesOeufs = ventesFiltrees.filter { it.getString("type") == "Œufs" }
            nbOeufs = ventesOeufs.sumOf { it.getLong("quantite")?.toInt() ?: 0 }
            revenuOeufs = ventesOeufs.sumOf {
                val qte = it.getLong("quantite")?.toInt() ?: 0
                val prix = it.getDouble("prixUnitaire") ?: 0.0
                qte * prix
            }

            // 🔹 Ventes de poules
            val ventesPoules = ventesFiltrees.filter { it.getString("type") == "Poule" }
            nbPoulesVendues = ventesPoules.size
            revenuPoules = ventesPoules.sumOf { it.getDouble("prixUnitaire") ?: 0.0 }

            // 🔹 Revenus mensuels
            ventesFiltrees.forEach {
                val mois = it.getString("date")?.take(7) ?: return@forEach
                val montant = when (it.getString("type")) {
                    "Œufs" -> {
                        val qte = it.getLong("quantite")?.toInt() ?: 0
                        val prix = it.getDouble("prixUnitaire") ?: 0.0
                        qte * prix
                    }
                    "Poule" -> it.getDouble("prixUnitaire") ?: 0.0
                    else -> 0.0
                }
                revenusParMois[mois] = (revenusParMois[mois] ?: 0.0) + montant
            }

            // 🔹 Dépenses par type et par mois
            val mapParType = mutableMapOf<String, Double>()
            evenementsSnap.documents.filter {
                val d = it.getString("date")?.let { dateStr -> LocalDate.parse(dateStr) } ?: return@filter false
                d in startDate..endDate
            }.forEach {
                val type = it.getString("type") ?: "Autre"
                val cout = it.getDouble("cout") ?: 0.0
                mapParType[type] = (mapParType[type] ?: 0.0) + cout

                val mois = it.getString("date")?.take(7) ?: return@forEach
                depensesParMois[mois] = (depensesParMois[mois] ?: 0.0) + cout
            }

            depensesParType = mapParType
            coutTotal = mapParType.values.sum()

            dataLoaded = true
        }
    }

    val revenuTotal = revenuOeufs + revenuPoules
    val resultatNet = revenuTotal - coutTotal
    val profitParOeuf = if (nbOeufs > 0) resultatNet / nbOeufs else 0.0
    val periode = "du ${startDate.format(formatter)} au ${endDate.format(formatter)}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFFBF6EC))
            .padding(16.dp)
    ) {
        Text("Rentabilité", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF5D4037))

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Plage de dates :", color = Color(0xFF5D4037))
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                selectDateRange(context, startDate, endDate) { start, end ->
                    startDate = start
                    endDate = end
                    dataLoaded = false
                }
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "Sélection date", tint = Color(0xFF5D4037))
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionTitle("1. Ventes $periode")
        StatCard("Œufs vendus", nbOeufs.toString())
        StatCard("Revenu des œufs (€)", "%.2f".format(revenuOeufs))
        StatCard("Poules vendues", nbPoulesVendues.toString())
        StatCard("Revenu des poules (€)", "%.2f".format(revenuPoules))

        Spacer(Modifier.height(24.dp))

        SectionTitle("2. Dépenses $periode")
        depensesParType.forEach { (type, valeur) ->
            StatCard("Dépense - $type", "%.2f €".format(valeur))
        }
        StatCard("Total des dépenses (€)", "%.2f".format(coutTotal))

        Spacer(Modifier.height(24.dp))

        SectionTitle("3. Résultats $periode")
        StatCard("Revenu total (€)", "%.2f".format(revenuTotal))
        StatCard("Résultat net (€)", "%.2f".format(resultatNet), color = if (resultatNet >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
        StatCard("Profit par œuf (€)", "%.2f".format(profitParOeuf))

        RentabiliteGraphSection(
            revenusParMois = revenusParMois,
            depensesParMois = depensesParMois,
            depensesParType = depensesParType,
            periode = periode,
            startDate = startDate,
            endDate = endDate
        )

    }
}


@Composable
fun StatCard(label: String, value: String, color: Color = Color(0xFF5D4037)) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF5D4037),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentabiliteGraphSection(
    revenusParMois: Map<String, Double>,
    depensesParMois: Map<String, Double>,
    depensesParType: Map<String, Double>,
    periode: String = "",
    startDate: LocalDate,
    endDate: LocalDate
) {
    val options = listOf(
        "Revenus mensuels",
        "Dépenses mensuelles",
        "Dépenses par type"
    )
    var selectedOption by remember { mutableStateOf(options[0]) }
    var expanded by remember { mutableStateOf(false) }

    val formatterMoisCourt = DateTimeFormatter.ofPattern("MMM") // "Jan", "Fév", etc.

    // Liste complète des mois de la période sélectionnée
    val moisPeriode = remember(startDate, endDate) {
        val list = mutableListOf<YearMonth>()
        var current = YearMonth.from(startDate)
        val endYM = YearMonth.from(endDate)
        while (!current.isAfter(endYM)) {
            list.add(current)
            current = current.plusMonths(1)
        }
        list
    }

    Column(Modifier.fillMaxWidth().padding(top = 24.dp)) {
        Text(
            "Graphique $periode",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037),
        )

        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                readOnly = true,
                value = selectedOption,
                onValueChange = {},
                label = { Text("Choisir un graphique") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        when (selectedOption) {
            "Revenus mensuels" -> {
                val labels = moisPeriode.map { it.format(formatterMoisCourt).replaceFirstChar { c -> c.uppercase() } }
                val values = moisPeriode.map {
                    revenusParMois[it.toString()]?.toFloat() ?: 0f
                }
                BarChartSection("Revenus mensuels", labels, values)
            }

            "Dépenses mensuelles" -> {
                val labels = moisPeriode.map { it.format(formatterMoisCourt).replaceFirstChar { c -> c.uppercase() } }
                val values = moisPeriode.map {
                    depensesParMois[it.toString()]?.toFloat() ?: 0f
                }
                BarChartSection("Dépenses mensuelles", labels, values)
            }

            "Dépenses par type" -> {
                val types = depensesParType.keys.toList()
                val valeurs = types.map { depensesParType[it]?.toFloat() ?: 0f }
                BarChartSection("Répartition des dépenses", types, valeurs)
            }
        }
    }
}

