package components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier


@Composable
fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label)
    }
}