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
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import com.example.myapplication.components.*
import com.example.myapplication.model.EvenementScreen
import com.example.myapplication.model.FicheIdentite
import com.google.common.math.Stats

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf("acceuil")}
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Menu(

                            onClose = {
                                println("Closing menu")
                                scope.launch { drawerState.close() } },
                            onItemSelected = {screen ->
                                println("Selected screen: $screen")
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                ){
                    Scaffold() { innerPadding ->
                        Column(
                            modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                        ){
                            Header(
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTitleClick = { currentScreen = "acceuil" },
                            )
                            when (currentScreen) {
                                "acceuil" -> Acceuil(onNavigate = {currentScreen = it})
                                "ficheIdentite" -> FicheIdentite()
                                //"stats" -> Stats()
                                "evenements" -> EvenementScreen(onMenuClick = { scope.launch { drawerState.open() } })
                            }
                        }
                    }
                }

            }
        }
    }
}

