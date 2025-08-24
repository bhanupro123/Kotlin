import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bhanupro.faceRecognition.lib.DeviceType
import com.bhanupro.faceRecognition.lib.DeviceTypeStorage
import com.bhanupro.faceRecognition.ui.global.UserRole
import com.bhanupro.faceRecognition.ui.navigation.LocalGlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlTopBar() {
    val globalViewModel = LocalGlobalViewModel.current
    val context = LocalContext.current
    val user by globalViewModel.userData.collectAsState()
    val deviceType by globalViewModel.deviceType.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Door Control") },
        actions = {
            if (user.role == UserRole.ADMIN) {
                IconButton(onClick = {
                    if (globalViewModel.userData.value.role == UserRole.ADMIN)
                        showMenu = true
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                DeviceType.NORMAL.name,
                                fontWeight = if (deviceType == DeviceType.NORMAL) FontWeight.Bold else FontWeight.Normal,
                                color = if (deviceType == DeviceType.NORMAL) MaterialTheme.colorScheme.primary else Color.Unspecified
                            )
                        },
                        onClick = {
                            DeviceTypeStorage.saveDeviceType(context, DeviceType.NORMAL.name)
                            globalViewModel.setDeviceType(DeviceType.NORMAL)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                DeviceType.SECURITY_MAIN.name,
                                fontWeight = if (deviceType == DeviceType.SECURITY_MAIN) FontWeight.Bold else FontWeight.Normal,
                                color = if (deviceType == DeviceType.SECURITY_MAIN) MaterialTheme.colorScheme.primary else Color.Unspecified
                            )
                        },
                        onClick = {
                            globalViewModel.setDeviceType(DeviceType.SECURITY_MAIN)
                            DeviceTypeStorage.saveDeviceType(context, DeviceType.SECURITY_MAIN.name)
                            showMenu = false
                        }
                    )
                }
            }
        }
    )
}
