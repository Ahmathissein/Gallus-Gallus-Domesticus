package com.example.myapplication.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.auth.AuthActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(onClose: () -> Unit, onItemSelected: (String) -> Unit) {
    MyApplicationTheme {
        ModalDrawerSheet(
            drawerContainerColor = Color(0xFFFBF5E9) // ton beige clair
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Menu",
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFFBF5E9)
                )
            )

            Divider(color = Color.LightGray, thickness = 1.dp)

            MenuItem(Icons.Default.Home, "Acceuil"){
                onItemSelected("acceuil")
                onClose()
            }
            val context = LocalContext.current

            MenuItem(Icons.Default.Logout, "Se déconnecter") {
                Firebase.auth.signOut() // Déconnecte l'utilisateur
                onClose() // Ferme le menu
                // Redirection vers l'écran d'authentification
                context.startActivity(Intent(context, AuthActivity::class.java))
            }


            MenuItem(Icons.Default.Search, "Rechercher"){}
        }
    }
}


@Composable
fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    MyApplicationTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label)
        }
    }
}