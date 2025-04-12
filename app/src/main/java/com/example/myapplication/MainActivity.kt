package com.example.myapplication

import Header
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.classes.poule.CreerPoule
import com.example.myapplication.classes.poule.DetailPouleScreen
import com.example.myapplication.classes.poule.Poule
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import com.example.myapplication.components.*
import com.example.myapplication.model.EvenementScreen
import com.example.myapplication.model.FicheIdentite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                var selectedPoule by remember { mutableStateOf<Poule?>(null) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Menu(
                            onClose = { scope.launch { drawerState.close() } },
                            onItemSelected = { screen ->
                                navController.navigate(screen)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Header(
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTitleClick = { navController.navigate("acceuil") }
                            )

                            NavHost(navController = navController, startDestination = "acceuil") {
                                composable("acceuil") {
                                    Acceuil(onNavigate = { navController.navigate(it) })
                                }

                                composable("ficheIdentite") {
                                    FicheIdentite(
                                        onAjouterPoule = { navController.navigate("creerPoule") },
                                        onVoirFiche = { poule ->
                                            selectedPoule = poule
                                            navController.navigate("detailPoule")
                                        }
                                    )

                                }

                                composable("creerPoule") {
                                    CreerPoule(
                                        onValider = { pouleCreee ->
                                            // Naviguer vers la fiche d'identité
                                            navController.navigate("ficheIdentite") {
                                                popUpTo("creerPoule") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable("evenements") {
                                    EvenementScreen(onMenuClick = { scope.launch { drawerState.open() } })
                                }

                                composable("detailPoule") {
                                    selectedPoule?.let { poule ->
                                        DetailPouleScreen(poule)
                                    }
                                }


                            }
                        }
                    }
                }
            }
        }

    }
}

