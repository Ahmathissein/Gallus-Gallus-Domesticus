package components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(onClose: () -> Unit) {
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

            MenuItem(Icons.Default.Home, "Accueil"){}
            MenuItem(Icons.Default.Person, "Fiches Identités"){}
            MenuItem(Icons.Default.BarChart, "Statistiques Pontes"){}
            MenuItem(Icons.Default.Event, "Événements"){}
            MenuItem(Icons.Default.AttachMoney, "Coûts"){}
            MenuItem(Icons.Default.Timeline, "Rentabilités"){}
            MenuItem(Icons.Default.Search, "Rechercher"){}
        }
    }
}
