package com.example.myapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.poulailler.poule.Ponte
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.poulailler.poule.Poule
import com.google.firebase.firestore.FieldValue
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SaisiePonte() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var poules by remember { mutableStateOf<List<Poule>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)
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

    val states = remember { mutableStateMapOf<String, Triple<Boolean, String, String>>() }

    LaunchedEffect(Unit) {
        firestore.collection("poules").document(userId).collection("liste")
            .get()
            .addOnSuccessListener { snapshot ->
                poules = snapshot.documents.mapNotNull { it.toObject(Poule::class.java) }
                isLoading = false
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .background(Color(0xFFFBF6EC))
    ) {
        Text(
            "Saisie rapide des pontes",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF5D4037)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = selectedDate.format(displayFormatter), color = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { datePickerDialog.show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700))
            ) {
                Icon(Icons.Default.EditCalendar, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Changer de date")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        poules.forEach { poule ->
            val (aPondu, nbOeufs, commentaire) = states[poule.id] ?: Triple(false, "", "")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = poule.photoPrincipaleUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.LightGray, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(poule.nom, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = aPondu, onCheckedChange = {
                            states[poule.id] = Triple(it, nbOeufs, commentaire)
                        })
                        Text("A pondu ?")
                        Spacer(Modifier.width(16.dp))
                        OutlinedTextField(
                            value = nbOeufs,
                            onValueChange = { states[poule.id] = Triple(aPondu, it, commentaire) },
                            label = { Text("Nombre d’œufs") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    OutlinedTextField(
                        value = commentaire,
                        onValueChange = { states[poule.id] = Triple(aPondu, nbOeufs, it) },
                        label = { Text("Commentaire") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Button(
            onClick = {
                val dateStr = selectedDate.format(dateFormatter)
                poules.forEach { poule ->
                    val (aPondu, nbOeufsStr, commentaire) = states[poule.id] ?: return@forEach
                    val nbOeufs = nbOeufsStr.toIntOrNull() ?: return@forEach
                    if (aPondu && nbOeufs > 0) {
                        val ponte = Ponte(dateStr, nbOeufs, commentaire.ifBlank { null })

                        // 🔸 Ajouter la ponte
                        val docRef = firestore.collection("poules")
                            .document(userId)
                            .collection("liste")
                            .document(poule.id)

                        // 🔸 Extraire mois et année
                        val year = selectedDate.year.toString()
                        val month = "${selectedDate.year}-${"%02d".format(selectedDate.monthValue)}"

                        // 🔸 Mettre à jour la ponte et incrémenter les stats
                        firestore.runBatch { batch ->
                            // Ajout dans le champ "pontes"
                            batch.update(docRef, "pontes.$dateStr", ponte)

                            // Incrément du mois
                            batch.update(docRef, "statistiquesMensuelles.$month", FieldValue.increment(nbOeufs.toLong()))

                            // Incrément de l'année
                            batch.update(docRef, "statistiquesAnnuelles.$year", FieldValue.increment(nbOeufs.toLong()))
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700))
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Enregistrer")
        }
    }
}
