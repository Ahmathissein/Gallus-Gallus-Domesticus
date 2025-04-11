package com.example.myapplication.model

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.myapplication.classes.poule.CreerPoule

@Composable
fun FicheIdentite() {
    var afficherFormulaire by remember { mutableStateOf(false) }
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
            onClick = {
                afficherFormulaire = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12))
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter une poule")
        }
    }
    if (afficherFormulaire) {
        CreerPoule(
            onValider = {pouleCreee ->
                afficherFormulaire = false
            }
        )
    }
}
