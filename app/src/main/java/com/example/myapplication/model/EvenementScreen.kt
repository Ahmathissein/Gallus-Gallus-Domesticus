package com.example.myapplication.model

import Header
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EvenementScreen(onMenuClick: () -> Unit) {
    val onglets = listOf("Tous", "Installation", "Entretien", "Réparations", "Prédation")
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var evenements by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {

        // 🧭 Titre + bouton créer
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Événements du\npoulailler",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037)
            )

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Créer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔘 Onglets stylisés
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEAE0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .padding(horizontal = 8.dp)
                            .height(3.dp),
                        color = Color(0xCC9C5700)
                    )
                },
                divider = {}
            ) {
                onglets.forEachIndexed { index, titre ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                titre,
                                color = if (selectedTab == index) Color(0xCC9C5700) else Color.Gray
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 Recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Rechercher…") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📋 Contenu
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Tous les événements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF5D4037)
                )
                Text(
                    "Historique complet des événements du poulailler",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                val filtered = evenements.filter {
                    val titre = (it["titre"] as? String ?: "").lowercase()
                    val description = (it["description"] as? String ?: "").lowercase()
                    val type = it["type"] as? String ?: ""
                    val query = searchQuery.lowercase()

                    (onglets[selectedTab] == "Tous" || type == onglets[selectedTab])
                            && (titre.contains(query) || description.contains(query))
                }
                Text("Affichage des événements pour l’onglet : ${onglets[selectedTab]}")

                when {
                    isLoading -> CircularProgressIndicator()
                    filtered.isEmpty() -> Text("Aucun événement trouvé.", color = Color.Gray)
                    else -> {
                        filtered.forEach { evt ->
                            EvenementCard(
                                titre = evt["titre"] as? String ?: "",
                                type = evt["type"] as? String ?: "",
                                date = evt["date"] as? String ?: "",
                                description = evt["description"] as? String ?: "",
                                onClickDetails = { /* Tu peux ouvrir un détail si tu veux */ }
                            )
                        }
                    }
                }

            }
        }
    }

    // 🧾 Modale de création d'événement (à compléter)
    if (showDialog) {

        DialogCreerEvenement(
            onDismiss = { showDialog = false },
            onSuccess = {
                showDialog = false
                // 🔁 Tu peux ajouter ici le rechargement de la liste
            }
        )
    }

    LaunchedEffect(Unit) {
        firestore.collection("evenements_poulailler")
            .document(userId)
            .collection("liste")
            .get()
            .addOnSuccessListener { snapshot ->
                evenements = snapshot.documents.mapNotNull { it.data }
                isLoading = false
            }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCreerEvenement(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val types = listOf("Installation", "Entretien", "Réparations", "Prédation")
    var titre by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cout by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer un nouvel événement") },
        text = {
            Column {
                OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Titre") })
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        label = { Text("Type d'événement") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (yyyy-MM-dd)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = cout, onValueChange = { cout = it }, label = { Text("Coût (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val erreur = when {
                    titre.isBlank() -> "Le titre est requis"
                    type.isBlank() -> "Le type est requis"
                    description.isBlank() -> "La description est requise"
                    date.isBlank() -> "La date est requise"
                    cout.toDoubleOrNull() == null -> "Le coût doit être un nombre"
                    else -> null
                }

                if (erreur != null) {
                    errorMessage = erreur
                    return@TextButton
                }

                val docId = "${date}_${System.currentTimeMillis()}"
                val evenement = mapOf(
                    "id" to docId,
                    "titre" to titre,
                    "type" to type,
                    "description" to description,
                    "date" to date,
                    "cout" to cout.toDouble()
                )

                firestore.collection("evenements_poulailler")
                    .document(userId)
                    .collection("liste")
                    .document(docId)
                    .set(evenement)
                    .addOnSuccessListener {
                        onSuccess()
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


@Composable
fun EvenementCard(
    titre: String,
    type: String,
    date: String,
    description: String,
    onClickDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4E7)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = titre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(4.dp))

            BadgeType(type = type)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onClickDetails,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE5CF)),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Détails", color = Color(0xFF5D4037))
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037)
            )
        }
    }
}


@Composable
fun BadgeType(type: String) {
    val badgeColor = when (type) {
        "Installation" -> Color(0xFF90CAF9)
        "Entretien" -> Color(0xFF81C784)
        "Réparations" -> Color(0xFFFFB74D)
        "Prédation" -> Color(0xFFE57373)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(color = badgeColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

