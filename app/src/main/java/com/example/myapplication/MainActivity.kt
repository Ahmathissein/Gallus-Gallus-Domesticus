package com.example.myapplication

import Header
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import components.Menu

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Menu(
                            onClose = { scope.launch { drawerState.close() } },
                        )
                    }

                ){
                    Scaffold(
                        topBar = {
                            Header(
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Text(
                            text = "Bienvenue",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

            }
        }
    }
}

