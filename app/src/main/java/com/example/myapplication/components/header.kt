
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(onMenuClick: () -> Unit = {}, onTitleClick: () -> Unit = {}){
    MyApplicationTheme {
        TopAppBar(
            title = {
                Text(text = "GGD",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onTitleClick)
                )
            },

            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                    )
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color(0xFFFEF9EC),
                titleContentColor = Color(0xFF5D4037),
                actionIconContentColor = Color(0xFF5D4037)
            )
        )
        Divider(color = Color.LightGray, thickness = 1.dp)
    }

}