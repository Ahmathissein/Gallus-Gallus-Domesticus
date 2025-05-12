import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.poulailler.poule.Poule
import com.example.myapplication.poulailler.poule.Vente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VenteScreen(onMenuClick: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Ajouter une vente", "Historique des ventes")

    var selectedPoule by remember { mutableStateOf<Poule?>(null) }
    var venteAModifier by remember { mutableStateOf<Vente?>(null) }
    var poules by remember { mutableStateOf<List<Poule>>(emptyList()) }

    // Charger les poules
    LaunchedEffect(Unit) {
        firestore.collection("poules").document(userId).collection("liste")
            .get().addOnSuccessListener { snapshot ->
                poules = snapshot.documents.mapNotNull { it.toObject(Poule::class.java) }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6EC))
            .padding(16.dp)
    ) {
        Text("Gestion des Ventes", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF5D4037))
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                VenteTab(
                    poules = poules,
                    venteExistante = venteAModifier,
                    onVenteValidee = {
                        selectedTab = 1
                        selectedPoule = null
                        venteAModifier = null
                    }
                )
            }
            1 -> {
                HistoriqueTab(onModify = { vente ->
                    venteAModifier = vente
                    selectedPoule = poules.find { it.id == vente.pouleId }
                    selectedTab = 0
                })
            }
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenteTab(
    poules: List<Poule>,
    venteExistante: Vente? = null,
    onVenteValidee: () -> Unit = {}
) {
    val poulesDisponibles = poules.filter {
        it.estVendue != true && it.dateDisparition == null && it.dateDeces == null
    }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val affichage = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var selectedDate by remember { mutableStateOf(venteExistante?.date?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var prixInput by remember { mutableStateOf(venteExistante?.prixUnitaire?.toString() ?: "") }
    var clientInput by remember { mutableStateOf(venteExistante?.client ?: "") }
    var quantite by remember { mutableStateOf(venteExistante?.quantite?.toString() ?: "") }
    var selectedPoule by remember {
        mutableStateOf(
            venteExistante?.pouleId?.let { id -> poules.find { it.id == id } }
        )
    }
    var selectedType by remember { mutableStateOf(venteExistante?.type ?: "") }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d -> selectedDate = LocalDate.of(y, m + 1, d) },
            selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
        )
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            if (venteExistante != null) "Modifier la vente" else "Nouvelle vente",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(16.dp))

        // Date
        OutlinedTextField(
            value = selectedDate.format(affichage),
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Type
        val types = listOf("Œufs", "Poule")
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type de vente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quantité ou Poule
        if (selectedType == "Œufs") {
            OutlinedTextField(
                value = quantite,
                onValueChange = { quantite = it },
                label = { Text("Quantité") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        } else if (selectedType == "Poule") {
            var pouleDropdownExpanded by remember { mutableStateOf(false) }

            Box {
                OutlinedTextField(
                    value = selectedPoule?.nom ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sélectionner une poule") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = pouleDropdownExpanded,
                    onDismissRequest = { pouleDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    poulesDisponibles.forEach { poule ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = poule.photoPrincipaleUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(36.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(poule.nom)
                                }
                            },
                            onClick = {
                                selectedPoule = poule
                                pouleDropdownExpanded = false
                            }
                        )
                    }

                }

                Box(
                    modifier = Modifier.matchParentSize().clickable { pouleDropdownExpanded = true }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Prix
        OutlinedTextField(
            value = prixInput,
            onValueChange = { prixInput = it },
            label = { Text("Prix unitaire (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Client
        OutlinedTextField(
            value = clientInput,
            onValueChange = { clientInput = it },
            label = { Text("Client (facultatif)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ✅ Bouton de validation
        Button(
            onClick = {
                if (selectedType.isBlank() || prixInput.isBlank() ||
                    (selectedType == "Œufs" && quantite.isBlank()) ||
                    (selectedType == "Poule" && selectedPoule == null)
                ) {
                    Toast.makeText(context, "Veuillez remplir tous les champs obligatoires.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val venteId = venteExistante?.id ?: "${selectedDate.format(formatter)}_${System.currentTimeMillis()}"
                val vente = Vente(
                    id = venteId,
                    type = selectedType,
                    date = selectedDate.format(formatter),
                    prixUnitaire = prixInput.toDoubleOrNull() ?: 0.0,
                    client = clientInput,
                    quantite = if (selectedType == "Œufs") quantite.toIntOrNull() else null,
                    pouleId = if (selectedType == "Poule") selectedPoule?.id else null
                )

                firestore.collection("ventes")
                    .document(userId)
                    .collection("liste")
                    .document(venteId)
                    .set(vente)
                    .addOnSuccessListener {
                        if (selectedType == "Poule" && selectedPoule != null) {
                            firestore.collection("poules")
                                .document(userId)
                                .collection("liste")
                                .document(selectedPoule!!.id)
                                .update("estVendue", true)
                        }
                        Toast.makeText(context, "✅ Vente enregistrée avec succès !", Toast.LENGTH_SHORT).show()
                        onVenteValidee()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700))
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Valider")
        }
    }
}



@Composable
fun HistoriqueTab(onModify: (Vente) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    var ventes by remember { mutableStateOf<List<Vente>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("ventes").document(userId).collection("liste")
            .get()
            .addOnSuccessListener { snapshot ->
                ventes = snapshot.documents.mapNotNull { it.toObject(Vente::class.java) }
                isLoading = false
            }
    }

    Column {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (ventes.isEmpty()) {
            Text("Aucune vente enregistrée", color = Color.Gray)
        } else {
            ventes.sortedByDescending { it.date }.forEach { vente ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Type : ${vente.type}", fontWeight = FontWeight.Bold)
                        Text("Date : ${vente.date}")
                        Text("Prix unitaire : ${vente.prixUnitaire} €")
                        vente.quantite?.let { Text("Quantité : $it") }
                        vente.client.takeIf { it.isNotBlank() }?.let { Text("Client : $it") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onModify(vente) }) {
                                Text("Modifier")
                            }
                        }
                    }
                }
            }
        }
    }
}
