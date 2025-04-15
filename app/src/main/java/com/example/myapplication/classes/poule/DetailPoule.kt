package com.example.myapplication.classes.poule

import Header
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.components.BarChartSection
import com.example.myapplication.firebase.ajouterDansFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.YearMonth


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPouleScreen(poule: Poule, navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Historique de ponte", "Album", "Événements")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        CenterAlignedTopAppBar(
            title = { Text("Fiche de ${poule.nom}", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = {
                    val gson = Gson()
                    val pouleJson = Uri.encode(gson.toJson(poule))
                    navController.navigate("creerPoule?json=$pouleJson")

                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier")
                }

            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 🐔 Photo principale
                Image(
                    painter = rememberAsyncImagePainter(poule.photoPrincipaleUri),
                    contentDescription = "Photo de ${poule.nom}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 🐤 Nom + variété
                Text(
                    text = poule.nom,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xCC9C5700)
                )
                Text(
                    text = poule.variete ?: "Variété inconnue",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 📝 Informations générales
                Text("Informations générales", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(4.dp))

                poule.dateNaissance?.let {
                    val naissanceTxt = if (poule.naissancePresumee) "Date de naissance (présumée)" else "Date de naissance"
                    Text("$naissanceTxt: ${it.format(DateTimeFormatter.ofPattern("MM/yyyy"))}", style = MaterialTheme.typography.bodySmall)
                }

                poule.dateAcquisition?.let {
                    Text("Date d’acquisition: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", style = MaterialTheme.typography.bodySmall)
                }

                if (!poule.lieuAchat.isNullOrBlank())
                    Text("Lieu d’acquisition: ${poule.lieuAchat}", style = MaterialTheme.typography.bodySmall)

                if (poule.prixAchat != null)
                    Text("Prix d’achat: ${poule.prixAchat} €", style = MaterialTheme.typography.bodySmall)

                poule.debutPonte?.let {
                    Text("Début de ponte: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🤎 Particularités
                Text("Particularités comportementales", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (!poule.particularitesComportementales.isNullOrBlank()) poule.particularitesComportementales!! else "Aucune",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEAE0)),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .padding(horizontal = 8.dp)
                            .height(3.dp),
                        color = Color(0xCC9C5700)
                    )
                },
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.History, contentDescription = null)
                                1 -> Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                2 -> Icon(Icons.Default.Event, contentDescription = null)

                            }
                        },
                        text = {
                            Text(
                                title,
                                color = if (selectedTabIndex == index) Color(0xCC9C5700) else Color.Gray
                            )
                        }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // 🟢 Onglet actif
        when (selectedTabIndex) {
            0 -> PonteTab(poule) { documentId, nb, com, dateStr ->
                ajouterDansFirestore(
                    pouleId = poule.id,
                    documentId = documentId,      // ex: "2025-04-13_1713209376"
                    date = dateStr,               // ex: "2025-04-13"
                    nbOeufs = nb,
                    commentaire = com,
                    onSuccess = {
                        println("Ponte ajoutée avec succès")
                    },
                    onError = {
                        println("Erreur ajout ponte : ${it.message}")
                    }
                )
            }
            1 -> AlbumTab(poule)
            2 -> EvenementTab(poule)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EvenementTab(poule: Poule) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var evenements by remember { mutableStateOf<List<Evenement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    // 🔄 Chargement initial
    LaunchedEffect(poule.id) {
        firestore.collection("poules")
            .document(userId)
            .collection("liste")
            .document(poule.id)
            .collection("evenements")
            .get()
            .addOnSuccessListener { snapshot ->
                evenements = snapshot.documents.mapNotNull { doc ->
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val type = doc.getString("type") ?: ""
                    val desc = doc.getString("description") ?: ""
                    Evenement(date, type, desc)
                }.sortedByDescending { it.date }
                isLoading = false
            }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Événements de santé et de vie", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
                Text("Historique des événements importants liés à ${poule.nom}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter un événement")
                }

                Spacer(Modifier.height(16.dp))

                when {
                    isLoading -> CircularProgressIndicator()
                    evenements.isEmpty() -> Text("Aucun événement enregistré", color = Color.Gray)
                    else -> {
                        evenements.forEach { evt ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4E7)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(evt.type, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                        Text(evt.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(evt.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔘 Formulaire ajout événement
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nouvel événement") },
            text = {
                Column {
                    Text("Date sélectionnée : ${selectedDate.format(dateFormatter)}")
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = { datePickerDialog.show() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C5700))) {
                        Text("Choisir la date", color = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type d’événement") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val dateStr = selectedDate.format(dateFormatter)
                    val docId = "${dateStr}_${System.currentTimeMillis()}"
                    val evt = mapOf(
                        "date" to dateStr,
                        "type" to type,
                        "description" to description
                    )

                    firestore.collection("poules")
                        .document(userId)
                        .collection("liste")
                        .document(poule.id)
                        .collection("evenements")
                        .document(docId)
                        .set(evt)
                        .addOnSuccessListener {
                            // Recharge la liste
                            firestore.collection("poules")
                                .document(userId)
                                .collection("liste")
                                .document(poule.id)
                                .collection("evenements")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    evenements = snapshot.documents.mapNotNull { doc ->
                                        val date = doc.getString("date") ?: return@mapNotNull null
                                        val type = doc.getString("type") ?: ""
                                        val desc = doc.getString("description") ?: ""
                                        Evenement(date, type, desc)
                                    }.sortedByDescending { it.date }
                                }
                        }

                    showDialog = false
                    type = ""
                    description = ""
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}



@Composable
fun AlbumTab(poule: Poule) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Album photo de ${poule.nom}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "Souvenirs visuels et moments marquants",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (poule.photos.isEmpty()) {
                    Text("Aucune photo dans l'album", color = Color.Gray)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp), // ajuste si besoin
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(poule.photos) { photo ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4E7))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(photo.uri),
                                        contentDescription = "Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = photo.date,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PonteTab(poule: Poule, onAjouterPonte: (String, Int, String?, String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    var pontes by remember { mutableStateOf<Map<String, Ponte>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentYear = java.time.Year.now().toString()
    val currentMonth = YearMonth.now().toString()

    var statsMensuelles by remember { mutableStateOf(List(12) { 0L }) }
    var statsAnnuelles by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }


    // 🔄 Chargement des pontes Firestore
    LaunchedEffect(poule.id) {
        val rawMensuelles = poule.statistiquesMensuelles ?: emptyMap()
        val rawAnnuelles: Map<String, Int> = poule.statistiquesAnnuelles ?: emptyMap()

        val moisFormates = listOf(
            "2025-01", "2025-02", "2025-03", "2025-04",
            "2025-05", "2025-06", "2025-07", "2025-08",
            "2025-09", "2025-10", "2025-11", "2025-12"
        )

        statsMensuelles = moisFormates.map { (rawMensuelles[it] ?: 0).toLong() }
        statsAnnuelles = rawAnnuelles.mapValues { it.value.toLong() }.toSortedMap()

        firestore.collection("poules")
            .document(userId)
            .collection("liste")
            .document(poule.id)
            .collection("pontes")
            .get()
            .addOnSuccessListener { snapshot ->
                val loaded = snapshot.documents.associate { doc ->
                    val date = doc.getString("date") ?: doc.id
                    val nb = doc.getLong("nbOeufs")?.toInt() ?: 0
                    val com = doc.getString("commentaire")
                    date to Ponte(date, nb, com)
                }
                pontes = loaded
                isLoading = false
            }
    }

    val today = LocalDate.now()
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    var selectedDate by remember { mutableStateOf(today) }
    var nbOeufs by remember { mutableStateOf("") }
    var commentaire by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Ponte de ${poule.nom}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
                Text("Enregistrements des œufs pondus", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ajouter une ponte")
                }

                Spacer(Modifier.height(16.dp))

                when {
                    isLoading -> CircularProgressIndicator()
                    pontes.isEmpty() -> Text("Aucune ponte enregistrée", color = Color.Gray)
                    else -> {
                        pontes.toSortedMap(reverseOrder()).forEach { (date, ponte) ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4E7)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.eggs),
                                        contentDescription = "Icône œuf",
                                        tint = Color(0xFF9C5700),
                                        modifier = Modifier.size(40.dp).padding(end = 12.dp)
                                    )
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(date, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                            Text("${ponte.nbOeufs} œufs", color = Color.Black)
                                        }
                                        if (!ponte.commentaire.isNullOrBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(ponte.commentaire ?: "", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                val mois = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")
                BarChartSection(
                    title = "Production mensuelle",
                    labels = mois,
                    values = statsMensuelles.map { it.toFloat() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                val decadeStart = (currentYear.toInt() / 10) * 10
                val annees = (decadeStart..decadeStart + 9).map { it.toString() }

                BarChartSection(
                    title = "Production annuelle",
                    labels = annees,
                    values = annees.map { statsAnnuelles[it] ?: 0L }.map { it.toFloat() }
                )

            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ajouter une ponte") },
            text = {
                Column {
                    Text("Date sélectionnée : ${selectedDate.format(dateFormatter)}")
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C5700))
                    ) { Text("Choisir une date", color = Color.White) }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nbOeufs,
                        onValueChange = { nbOeufs = it },
                        label = { Text("Nombre d'œufs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commentaire,
                        onValueChange = { commentaire = it },
                        label = { Text("Commentaire (facultatif)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nb = nbOeufs.toIntOrNull()
                    if (nb != null && nb >= 0) {
                        val dateStr = selectedDate.format(dateFormatter)
                        val docId = "${dateStr}_${System.currentTimeMillis()}"
                        onAjouterPonte(docId, nb, commentaire.ifBlank { null }, dateStr)

                        // 🔁 Recharge les pontes après ajout
                        firestore.collection("poules")
                            .document(userId)
                            .collection("liste")
                            .document(poule.id)
                            .collection("pontes")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                pontes = snapshot.documents.associate { doc ->
                                    val docDate = doc.getString("date") ?: doc.id
                                    val n = doc.getLong("nbOeufs")?.toInt() ?: 0
                                    val c = doc.getString("commentaire")
                                    docDate to Ponte(docDate, n, c)
                                }
                            }

                        showDialog = false
                        nbOeufs = ""
                        commentaire = ""
                    }
                }) {
                    Text("Valider")
                }
            }
            ,
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
