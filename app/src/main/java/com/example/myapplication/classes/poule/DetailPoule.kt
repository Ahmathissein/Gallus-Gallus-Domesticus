package com.example.myapplication.classes.poule

import Header
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPouleScreen(poule: Poule) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Album", "Événements", "Statistiques")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        CenterAlignedTopAppBar(
            title = { Text("Fiche de ${poule.nom}", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { /* retour */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                IconButton(onClick = { /* modifier */ }) {
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
                                0 -> Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                1 -> Icon(Icons.Default.Event, contentDescription = null)
                                2 -> Icon(Icons.Default.BarChart, contentDescription = null)
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
            0 -> AlbumTab(poule)
            1 -> EvenementTab(poule)
            //2 -> StatsTab(poule)
        }
    }
}

@Composable
fun EvenementTab(poule: Poule) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 16.dp)) {

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Événements de santé et de vie",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "Historique des événements importants liés à ${poule.nom}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tu pourras remplacer cette liste par poule.evenements si disponible
                val evenements = listOf(
                    Triple("Maladie", "Légère toux, traitée avec remède naturel", "15/08/2023"),
                    Triple("Traitement préventif", "Vermifuge naturel", "05/09/2023"),
                    Triple("Examen", "Santé parfaite, plumage brillant", "10/11/2023")
                )

                evenements.forEach { (titre, description, date) ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4E7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(titre, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AlbumTab(poule: Poule) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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

