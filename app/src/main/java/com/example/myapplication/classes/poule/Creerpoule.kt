package com.example.myapplication.classes.poule

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter


@Composable
fun CreerPoule(
    onValider: (Poule) -> Unit
) {
    val scrollState = rememberScrollState()

    // Champs avec état
    var nom by remember { mutableStateOf("") }
    var variete by remember { mutableStateOf("") }
    var lieuAchat by remember { mutableStateOf("") }
    var prixAchat by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var dateNaissancePresumee by remember { mutableStateOf(false) }
    var dateAcquisition by remember { mutableStateOf("") }
    var dateAcquisitionPresumee by remember { mutableStateOf(false) }
    var particularites by remember { mutableStateOf("") }
    var debutPonte by remember { mutableStateOf("") }
    var causeDeces by remember { mutableStateOf("") }
    var causePresumee by remember { mutableStateOf(false) }
    var dateDeces by remember { mutableStateOf("") }
    var dateDecesPresumee by remember { mutableStateOf(false) }
    var photoPrincipaleUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){
        uri: Uri? ->
        photoPrincipaleUri = uri
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Créer une fiche poule",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom de la poule") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dateAcquisition,
            onValueChange = { dateAcquisition = it },
            label = { Text("Date d'acquisition (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateAcquisitionPresumee,
                onCheckedChange = { dateAcquisitionPresumee = it }
            )
            Text(text = "Date d'acquisition présumée")
        }

        OutlinedTextField(
            value = lieuAchat,
            onValueChange = { newValue -> lieuAchat = newValue },
            label = { Text("Lieu d'achat") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = prixAchat,
            onValueChange = { prixAchat = it },
            label = { Text("Prix d'achat (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dateNaissance,
            onValueChange = { dateNaissance = it },
            label = { Text("Date de naissance (yyyy-mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateNaissancePresumee,
                onCheckedChange = { dateNaissancePresumee = it }
            )
            Text(text = "Date de naissance présumée")
        }


        OutlinedTextField(
            value = variete,
            onValueChange = { variete = it },
            label = { Text("Variété") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = debutPonte,
            onValueChange = { debutPonte = it },
            label = { Text("Début de ponte (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = particularites,
            onValueChange = { particularites = it },
            label = { Text("Particularités comportementales") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = dateDeces,
            onValueChange = { dateDeces = it },
            label = { Text("Date de deces (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateDecesPresumee,
                onCheckedChange = { dateDecesPresumee = it }
            )
            Text(text = "Date de deces présumée")
        }

        OutlinedTextField(
            value = causeDeces,
            onValueChange = { causeDeces = it },
            label = { Text("Cause du décès") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = causePresumee, onCheckedChange = { causePresumee = it })
            Text("Cause présumée")
        }

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choisir une image principale", color = Color.White)
        }

        photoPrincipaleUri?.let { uri ->
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Photo principale",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                // Tu peux construire un objet Poule ici avec les champs saisis
                // Exemple basique :
                val poule = Poule(
                    nom = nom,
                    variete = variete,
                    lieuAchat = lieuAchat,
                    prixAchat = prixAchat.toDoubleOrNull(),
                    naissancePresumee = dateNaissancePresumee,
                    particularitesComportementales = particularites
                )
                onValider(poule)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12))
        ) {
            Text("Créer la fiche", color = Color.White)
        }
    }
}
