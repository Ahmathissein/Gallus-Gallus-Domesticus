package com.example.myapplication.components

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun Acceuil (onNavigate: (String) -> Unit) {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFfbf6ec))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Gallus Gallus Domesticus",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Votre outil de gestion pour la basse-cour familiale",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(24.dp))
            /*
                    Row(horizontalArrangement = Arrangement.SpaceBetween){
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)) ){
                            Text("Commencer")
                        }
                    }
            */
            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                //contentPadding = paddingValues(4.dp)
            ){
                item {
                    AccueilCard(
                        icon = painterResource(id = R.drawable.chicken),
                        title = "Mes\nPoules",
                        description = "Informations pour\nchaque gallinacé",
                        onClick = {
                            onNavigate("ficheIdentite")
                        }
                    )
                }

                item {
                    AccueilCard(
                        icon = painterResource(id = R.drawable.eggs),
                        title = "Saisie\nde ponte",
                        description = "Ajouter les œufs pondus par jour",
                        onClick = {
                            onNavigate("saisiePonte")
                        }
                    )
                }


                item {
                    AccueilCard(
                        icon = rememberVectorPainter(Icons.Default.BarChart),
                        title = "Statistiques globales",
                        description = "Analyse de production d'oeufs du poulailler",
                        onClick = {
                            onNavigate("stats")
                        }
                    )
                }

                item {
                    AccueilCard(
                        icon = rememberVectorPainter(Icons.Default.Event),
                        title = "Événements du poulailler",
                        description = "Événements importants de la basse-cour",
                        onClick = {
                            onNavigate("evenements")
                        }
                    )
                }

                item {
                    AccueilCard(
                        icon = rememberVectorPainter(Icons.Default.AttachMoney),
                        title = "Vente de produits",
                        description = "Traces des dépenses liées à l'élevage",
                        onClick = {
                            onNavigate("vente")
                        }
                    )
                }

                item {
                    AccueilCard(
                        icon = rememberVectorPainter(Icons.Default.Timeline),
                        title = "Rentabilité",
                        description = "Retour sur investissement de la basse-cour",
                        onClick = {
                            onNavigate("rentabilite")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AccueilCard(icon: Painter, title: String, description: String, onClick: () -> Unit){
    MyApplicationTheme {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xCC9C5700))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xCC9C5700),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Accéder",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

