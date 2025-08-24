import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.sp
import com.bhanupro.faceRecognition.ui.global.GlobalViewModel
import com.bhanupro.faceRecognition.ui.global.UserData

@Composable
fun UserCard(user: UserData,globalViewModel: GlobalViewModel, modifier: Modifier = Modifier) {
    val deviceType by globalViewModel.deviceType.collectAsState()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (user.avatar != null) {
                Image(
                    bitmap = user.avatar.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Default Avatar",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(user.username, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { user.role?.name?.let { Text(it) } }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Login via ${user.loginMode} Device Type ${deviceType.name}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

