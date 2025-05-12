package com.example.myapplication.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.poulailler.poule.Ponte
import com.example.myapplication.poulailler.poule.Poule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    val states = remember { mutableStateMapOf<String, Triple<Boolean, Int, Int>>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        firestore.collection("poules").document(userId).collection("liste")
            .get()
            .addOnSuccessListener { snapshot ->
                val toutes = snapshot.documents.mapNotNull { it.toObject(Poule::class.java) }
                poules = toutes.filter {
                    it.estVendue != true && it.dateDisparition == null && it.dateDeces == null
                }
                println(" Toutes les poules : ${toutes.map { it.nom to it.estVendue }}")
                println(" Poules non vendues : ${poules.map { it.nom }}")
                isLoading = false
            }
    }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF6EC))
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Saisie rapide des pontes",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(displayFormatter),
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
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

            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 0.dp,
                color = Color(0xFFFFF8E1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Photo", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Nom", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("A pondu ?", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Mauvais œuf ?", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Nombre d'œufs", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    Divider(color = Color(0xFFDDD0C8), thickness = 1.dp)

                    poules.forEach { poule ->
                        val (aPondu, nbOeufs, nbOeufsMauvais) = states[poule.id] ?: Triple(false, 0, 0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = poule.photoPrincipaleUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop, // Important pour garder un beau cadrage
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape) // Découpe l'image en cercle
                                    .border(1.dp, Color.Gray, CircleShape) // Optionnel : ajoute une bordure fine
                                    .weight(0.8f)
                            )



                            Spacer(modifier = Modifier.width(12.dp)) // espace entre photo et nom

                            Text(poule.nom, modifier = Modifier.weight(1.2f), fontSize = 15.sp)

                            Spacer(modifier = Modifier.width(12.dp))

                            Checkbox(
                                checked = aPondu,
                                onCheckedChange = { checked ->
                                    val (_, _, oeufMauvais) = states[poule.id] ?: Triple(false, 0, 0)
                                    val newNbOeufs: Int = if (checked) if (nbOeufs == 0) 1 else nbOeufs else 0
                                    val newOeufMauvais: Int = if (!checked) 0 else oeufMauvais
                                    states[poule.id] = Triple(checked, newNbOeufs, newOeufMauvais)

                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xCC9C5700)),
                                modifier = Modifier.weight(1f)
                            )


                            Spacer(modifier = Modifier.width(12.dp))

                            val scope = rememberCoroutineScope() // à placer plus haut dans la composable si pas déjà fait

                            val nbOeufsInt = nbOeufs
                            val nbOeufsMauvais = if (states.containsKey(poule.id)) {
                                states[poule.id]?.third?.toString()?.toIntOrNull() ?: 0
                            } else 0

                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(45.dp)
                                    .border(1.dp, Color.Red, RoundedCornerShape(6.dp))
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = nbOeufsMauvais.toString(), fontSize = 16.sp, color = Color.Red)
                                    Column(
                                        modifier = Modifier.padding(start = 6.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (aPondu && nbOeufsMauvais < nbOeufsInt) {
                                                    states[poule.id] = Triple(aPondu, nbOeufs, nbOeufsMauvais + 1)
                                                }
                                            },
                                            enabled = aPondu && nbOeufsInt > 0,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus")
                                        }
                                        IconButton(
                                            onClick = {
                                                if (aPondu && nbOeufsMauvais > 0) {
                                                    states[poule.id] = Triple(aPondu, nbOeufs, nbOeufsMauvais - 1)
                                                }
                                            },
                                            enabled = aPondu && nbOeufs > 0 && nbOeufsMauvais > 0,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Moins")
                                        }


                                    }
                                }
                            }






                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(45.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = nbOeufs.toString(), fontSize = 16.sp)
                                    Column(
                                        modifier = Modifier.padding(start = 6.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val current: Int = nbOeufs
                                                val updated = current + 1
                                                val newAPondu = true
                                                val oeufMauvais = states[poule.id]?.third ?: 0
                                                states[poule.id] = Triple(newAPondu, updated, oeufMauvais)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus")
                                        }


                                        IconButton(
                                            onClick = {
                                                val current: Int = nbOeufs
                                                val updated: Int = maxOf(0, current - 1)
                                                val newAPondu: Boolean = updated > 0
                                                val oeufMauvais = states[poule.id]?.third ?: 0
                                                val newOeufMauvais: Int = minOf(oeufMauvais, updated)

                                                states[poule.id] = Triple(newAPondu, updated, newOeufMauvais)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Moins")
                                        }



                                    }
                                }
                            }




                        }
                        Divider(color = Color(0xFFF0E8E0), thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val dateStr = selectedDate.format(dateFormatter)
                    var errorDetected = false
                    var errorMessage = ""
                    var saveMade = false

                    poules.forEach { poule ->
                        val (aPondu, nbOeufs, nbOeufsMauvais) = states[poule.id] ?: return@forEach

                        if (!aPondu && nbOeufs > 0) {
                            errorDetected = true
                            errorMessage = "⚠ Veuillez cocher 'A pondu ?' si vous avez indiqué un nombre d'œufs."
                            return@forEach
                        }

                        if (aPondu && nbOeufs == 0) {
                            errorDetected = true
                            errorMessage = "⚠ Veuillez indiquer un nombre d'œufs si vous avez coché 'A pondu ?'."
                            return@forEach
                        }

                        if (aPondu && nbOeufs > 0) {
                            val ponte = Ponte(dateStr, nbOeufs, nbOeufsMauvais)
                            val docRef = firestore.collection("poules")
                                .document(userId)
                                .collection("liste")
                                .document(poule.id)

                            val year = selectedDate.year.toString()
                            val month = "${selectedDate.year}-${"%02d".format(selectedDate.monthValue)}"

                            firestore.runBatch { batch ->
                                batch.update(docRef, "pontes.$dateStr", ponte)
                                batch.update(docRef, "statistiquesMensuelles.$month", FieldValue.increment(nbOeufs.toLong()))
                                batch.update(docRef, "statistiquesAnnuelles.$year", FieldValue.increment(nbOeufs.toLong()))
                            }
                            saveMade = true
                        }
                    }

                    coroutineScope.launch {
                        when {
                            errorDetected -> snackbarHostState.showSnackbar(errorMessage)
                            saveMade -> snackbarHostState.showSnackbar("✅ Données enregistrées avec succès !")
                            else -> snackbarHostState.showSnackbar("Aucune donnée à enregistrer.")
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700))
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Enregistrer")
            }
        }
    }
}