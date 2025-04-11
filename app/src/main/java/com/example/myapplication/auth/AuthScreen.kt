package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Connexion", "Inscription")

    // Fond général beige clair
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6EC))
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Barre d'onglets stylisée
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF5D4037),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[tabIndex])
                                .height(3.dp),
                            color = Color(0xCC9C5700)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = {
                                Text(
                                    title,
                                    color = if (tabIndex == index) Color(0xCC9C5700) else Color.Gray,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contenu du formulaire selon l'onglet
            when (tabIndex) {
                0 -> LoginForm(onAuthSuccess)
                1 -> SignupForm(
                    onAuthSuccess = { /* ne fait rien ici */ },
                    switchToLoginTab = { tabIndex = 0 }
                )
            }
        }
    }
}
