package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.poulailler.poule.Poule
import java.time.format.DateTimeFormatter


@Composable
fun FicheIdentite(onAjouterPoule: () -> Unit, onVoirFiche: (Poule) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var poules by remember {mutableStateOf<List<Poule>>(emptyList())}

    LaunchedEffect(userId) {
        userId?.let { uid ->
            firestore.collection("poules")
            .document(uid)
                .collection("liste")
                .get()
                .addOnSuccessListener {result ->
                    poules = result.mapNotNull { it.toObject(Poule::class.java) }
                }
                .addOnFailureListener {
                    println("Erreur Firestore: ${it.message}")
                }
        }
    }

    Column(
        modifier = Modifier
        .fillMaxSize()
            .background(Color(0xFFFBF6EC))
            .padding(16.dp),
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fiches\nd'identité",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { onAjouterPoule() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter une poule")
            }
        }
        if (poules.isEmpty()) {
            Text("Aucune poule trouvée", color = Color.Gray)
        }else{
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                items(poules){ poule ->
                    PouleCard(poule = poule, onVoirFiche = onVoirFiche)

                }
            }
        }
    }

}

@Composable
fun PouleCard(poule: Poule, onVoirFiche: (Poule) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){
        Column(
            modifier = Modifier.padding(12.dp)
        ){
            Image(painter = rememberAsyncImagePainter(poule.photoPrincipaleUri),
                contentDescription = "Photo ${poule.nom}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = poule.nom,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xCC9C5700)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Variété : ${poule.variete ?: "Non spécifiée"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            poule.dateNaissance?.let {
                val libelleDate = if (poule.naissancePresumee) "Date de naissance présumée" else "Date de naissance"
                val dateFormat = it.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                Text("$libelleDate : $dateFormat", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onVoirFiche(poule)},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voir la fiche", color = Color.White)
            }
        }
    }
}
