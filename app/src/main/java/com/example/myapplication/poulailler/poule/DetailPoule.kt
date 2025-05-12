package com.example.myapplication.poulailler.poule

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
    var showDisabledInfoDialog by remember { mutableStateOf(false) }
    var disabledInfoMessage by remember { mutableStateOf("") }

    val tabTitles = listOf("Statistiques de ponte", "Album", "Événements")
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
                Text("Statut de la poule", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
                if (poule.estVendue) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, tint = Color.Gray, contentDescription = "Vendue")
                        Text("Vendue", modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.Medium)
                    }
                }else {
                    var showFormDisparition by remember { mutableStateOf(false) }
                    var showConfirmDeleteDisparition by remember { mutableStateOf(false) }
                    var disparitionChecked by remember { mutableStateOf(poule.dateDisparition != null) }

                    var showFormDeces by remember { mutableStateOf(false) }
                    var showConfirmDeleteDeces by remember { mutableStateOf(false) }
                    var decesChecked by remember { mutableStateOf(poule.dateDeces != null) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = disparitionChecked,
                            onCheckedChange = { checked ->
                                if (decesChecked) {
                                    disabledInfoMessage = "Impossible de marquer la poule comme disparue si elle est déclarée décédée."
                                    showDisabledInfoDialog = true
                                    return@Checkbox
                                }

                                if (checked) showFormDisparition = true
                                else showConfirmDeleteDisparition = true
                            },
                            enabled = true // Toujours cliquable, on gère le blocage à la main
                        )
                        Text("Disparue", modifier = Modifier.padding(start = 4.dp))
                    }


                    if (showFormDisparition) {
                        FormulaireDisparition(
                            onDismiss = { showFormDisparition = false },
                            onValider = { date, datePresumee, cause, causePresumee ->
                                val docRef = FirebaseFirestore.getInstance()
                                    .collection("poules")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection("liste")
                                    .document(poule.id)

                                val maj = mapOf(
                                    "dateDisparition" to date,
                                    "dateDisparitionPresumee" to datePresumee,
                                    "causeDisparition" to cause,
                                    "causeDisparitionPresumee" to causePresumee
                                )

                                docRef.update(maj).addOnSuccessListener {
                                    disparitionChecked = true
                                    showFormDisparition = false
                                }
                            }
                        )
                    }

                    if (showConfirmDeleteDisparition) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDeleteDisparition = false },
                            title = { Text("Supprimer la déclaration de disparition ?") },
                            text = { Text("Cela supprimera la date et la cause de disparition enregistrées.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    val docRef = FirebaseFirestore.getInstance()
                                        .collection("poules")
                                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .collection("liste")
                                        .document(poule.id)

                                    val updates = mapOf(
                                        "dateDisparition" to null,
                                        "dateDisparitionPresumee" to null,
                                        "causeDisparition" to null,
                                        "causeDisparitionPresumee" to null
                                    )

                                    docRef.update(updates).addOnSuccessListener {
                                        disparitionChecked = false
                                        showConfirmDeleteDisparition = false
                                    }
                                }) {
                                    Text("Confirmer")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showConfirmDeleteDisparition = false
                                }) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = decesChecked,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showFormDeces = true
                                } else {
                                    showConfirmDeleteDeces = true
                                }
                            }
                        )
                        Text("Décédée", modifier = Modifier.padding(start = 4.dp))
                    }
                    if (showFormDeces) {
                        FormulaireDeces(
                            onDismiss = { showFormDeces = false },
                            onValider = { dateStr, datePresumee, cause, causePresumee ->
                                val docRef = FirebaseFirestore.getInstance().collection("poules")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection("liste")
                                    .document(poule.id)

                                docRef.update(
                                    mapOf(
                                        "dateDeces" to dateStr,
                                        "dateDecesPresumee" to datePresumee,
                                        "causeDeces" to cause,
                                        "causePresumee" to causePresumee,
                                        "modifieLe" to com.google.firebase.Timestamp.now()
                                    )
                                ).addOnSuccessListener {
                                    decesChecked = true
                                }
                            }
                        )
                    }

                    if (showConfirmDeleteDeces) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDeleteDeces = false },
                            title = { Text("Annuler le décès") },
                            text = { Text("Voulez-vous supprimer les informations de décès ?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    val docRef = FirebaseFirestore.getInstance().collection("poules")
                                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .collection("liste")
                                        .document(poule.id)

                                    docRef.update(
                                        mapOf(
                                            "dateDeces" to null,
                                            "dateDecesPresumee" to null,
                                            "causeDeces" to null,
                                            "causePresumee" to null,
                                            "modifieLe" to com.google.firebase.Timestamp.now()
                                        )
                                    ).addOnSuccessListener {
                                        decesChecked = false
                                        showConfirmDeleteDeces = false
                                    }
                                }) {
                                    Text("Supprimer")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showConfirmDeleteDeces = false
                                }) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }

                    if (showDisabledInfoDialog) {
                        AlertDialog(
                            onDismissRequest = { showDisabledInfoDialog = false },
                            title = { Text("Action impossible") },
                            text = { Text(disabledInfoMessage) },
                            confirmButton = {
                                TextButton(onClick = { showDisabledInfoDialog = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }


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
                                0 -> Icon(Icons.Filled.BarChart, contentDescription = null)
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
            0 -> PonteTab(poule)
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
    var showConfirmDelete by remember { mutableStateOf(false) }
    var evenementASupprimer by remember { mutableStateOf<Evenement?>(null) }

    var showConfirmEdit by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var evenements by remember { mutableStateOf<List<Evenement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var evenementEnCoursId by remember { mutableStateOf<String?>(null) }
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(evt.type, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                            Text(evt.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }

                                        Row {
                                            IconButton(onClick = {
                                                selectedDate = LocalDate.parse(evt.date)
                                                type = evt.type
                                                description = evt.description
                                                evenementEnCoursId = "${evt.date}_${System.currentTimeMillis()}" // temporaire
                                                showDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Modifier")
                                            }

                                            IconButton(onClick = {
                                                evenementASupprimer = evt
                                                showConfirmDelete = true
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                                            }
                                        }
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
            title = { Text(if (evenementEnCoursId != null) "Modifier l’événement" else "Nouvel événement") },
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

                    val evtData = mapOf(
                        "date" to dateStr,
                        "type" to type,
                        "description" to description
                    )

                    if (evenementEnCoursId != null) {
                        showConfirmEdit = true  // 👉 active la boîte de confirmation
                    } else {
                        val docId = "${dateStr}_${System.currentTimeMillis()}"
                        firestore.collection("poules")
                            .document(userId)
                            .collection("liste")
                            .document(poule.id)
                            .collection("evenements")
                            .document(docId)
                            .set(evtData)
                            .addOnSuccessListener {
                                reloadEvenements(poule.id, firestore, userId) { evenements = it }
                            }

                        showDialog = false
                        type = ""
                        description = ""
                    }
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

    // ✏️ Confirmation modification
    if (showConfirmEdit && evenementEnCoursId != null) {
        AlertDialog(
            onDismissRequest = { showConfirmEdit = false },
            title = { Text("Confirmation") },
            text = { Text("Enregistrer les modifications ?") },
            confirmButton = {
                TextButton(onClick = {
                    val dateStr = selectedDate.format(dateFormatter)
                    val evtData = mapOf(
                        "date" to dateStr,
                        "type" to type,
                        "description" to description
                    )

                    firestore.collection("poules")
                        .document(userId)
                        .collection("liste")
                        .document(poule.id)
                        .collection("evenements")
                        .document(evenementEnCoursId!!)
                        .set(evtData)
                        .addOnSuccessListener {
                            evenementEnCoursId = null
                            showConfirmEdit = false
                            reloadEvenements(poule.id, firestore, userId) { evenements = it }
                            showDialog = false
                            type = ""
                            description = ""
                        }
                }) {
                    Text("Oui")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmEdit = false
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showConfirmDelete && evenementASupprimer != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Supprimer l’événement") },
            text = { Text("Voulez-vous vraiment supprimer cet événement ?") },
            confirmButton = {
                TextButton(onClick = {
                    firestore.collection("poules")
                        .document(userId)
                        .collection("liste")
                        .document(poule.id)
                        .collection("evenements")
                        .whereEqualTo("date", evenementASupprimer!!.date)
                        .whereEqualTo("type", evenementASupprimer!!.type)
                        .whereEqualTo("description", evenementASupprimer!!.description)
                        .get()
                        .addOnSuccessListener { result ->
                            val docId = result.documents.firstOrNull()?.id
                            if (docId != null) {
                                firestore.collection("poules")
                                    .document(userId)
                                    .collection("liste")
                                    .document(poule.id)
                                    .collection("evenements")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener {
                                        reloadEvenements(poule.id, firestore, userId) { evenements = it }
                                    }
                            }
                            showConfirmDelete = false
                            evenementASupprimer = null
                        }
                }) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDelete = false
                    evenementASupprimer = null
                }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PonteTab(poule: Poule) {
    val currentYear = java.time.Year.now().toString()
    var statsMensuelles by remember { mutableStateOf(List(12) { 0L }) }
    var statsAnnuelles by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var selectedGraph by remember { mutableStateOf("Mensuel") }

    val options = listOf("Mensuel", "Annuel")
    var expanded by remember { mutableStateOf(false) }

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
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 🔽 Sélecteur de graphique
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedGraph,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de graphique") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
                                selectedGraph = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 📊 Graphique choisi
            if (selectedGraph == "Mensuel") {
                val mois = listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc")
                BarChartSection(
                    title = "Production mensuelle",
                    labels = mois,
                    values = statsMensuelles.map { it.toFloat() }
                )
            } else {
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
}



fun reloadEvenements(
    pouleId: String,
    firestore: FirebaseFirestore,
    userId: String,
    onResult: (List<Evenement>) -> Unit
) {
    firestore.collection("poules")
        .document(userId)
        .collection("liste")
        .document(pouleId)
        .collection("evenements")
        .get()
        .addOnSuccessListener { snapshot ->
            val events = snapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: return@mapNotNull null
                val type = doc.getString("type") ?: ""
                val desc = doc.getString("description") ?: ""
                Evenement(date, type, desc)
            }.sortedByDescending { it.date }
            onResult(events)
        }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FormulaireDisparition(
    onDismiss: () -> Unit,
    onValider: (String, Boolean, String, Boolean) -> Unit,
    dateInitiale: String? = null,
    causeInitiale: String? = null
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    var datePresumee by remember { mutableStateOf(false) }
    var cause by remember { mutableStateOf(causeInitiale ?: "") }
    var causePresumee by remember { mutableStateOf(false) }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Déclarer la disparition") },
        text = {
            Column {
                Text("Date de disparition : ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                Button(onClick = { datePickerDialog.show() }) {
                    Text("Choisir la date")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = datePresumee, onCheckedChange = { datePresumee = it })
                    Text("Date présumée", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = cause, onValueChange = { cause = it }, label = { Text("Cause") })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = causePresumee, onCheckedChange = { causePresumee = it })
                    Text("Cause présumée", modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (cause.isNotBlank()) {
                    onValider(dateStr, datePresumee, cause, causePresumee)
                    onDismiss()
                }
            }) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FormulaireDeces(
    onDismiss: () -> Unit,
    onValider: (String, Boolean, String, Boolean) -> Unit,
    dateInitiale: String? = null,
    causeInitiale: String? = null
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    var datePresumee by remember { mutableStateOf(false) }
    var cause by remember { mutableStateOf(causeInitiale ?: "") }
    var causePresumee by remember { mutableStateOf(false) }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Déclarer le décès") },
        text = {
            Column {
                Text("Date de décès : ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                Button(onClick = { datePickerDialog.show() }) {
                    Text("Choisir la date")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = datePresumee, onCheckedChange = { datePresumee = it })
                    Text("Date présumée", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = cause, onValueChange = { cause = it }, label = { Text("Cause") })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = causePresumee, onCheckedChange = { causePresumee = it })
                    Text("Cause présumée", modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (cause.isNotBlank()) {
                    onValider(dateStr, datePresumee, cause, causePresumee)
                    onDismiss()
                }
            }) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
