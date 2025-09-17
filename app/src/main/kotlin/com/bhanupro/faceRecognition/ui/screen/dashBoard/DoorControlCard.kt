import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bhanupro.faceRecognition.app.SnackbarManager
import com.bhanupro.faceRecognition.lib.DeviceIpStorage
import com.bhanupro.faceRecognition.lib.DeviceType
import com.bhanupro.faceRecognition.lib.DeviceTypeStorage
import com.bhanupro.faceRecognition.ui.global.GlobalViewModel
import com.bhanupro.faceRecognition.ui.global.UserData
import com.bhanupro.faceRecognition.ui.global.UserRole
import com.bhanupro.faceRecognition.ui.navigation.LocalGlobalViewModel
enum class DoorAction {
    LOCK, CALL, LOGIN, DEVICES, LOGOUT
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlTopBar( onAction: (DoorAction) -> Unit) {
    val globalViewModel = LocalGlobalViewModel.current
    val context = LocalContext.current
    val user by globalViewModel.userData.collectAsState()
    val deviceType by globalViewModel.deviceType.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Door Control") },
        actions = {

            if (showDialog) {

                var ip by remember { mutableStateOf(DeviceIpStorage.getDeviceIp(context) ?: "") }
                var dialogIp by remember { mutableStateOf(ip) }
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Enter IP Address") },
                    text = {
                        TextField(
                            value = dialogIp,
                            onValueChange = { dialogIp = it },
                            label = { Text("New IP") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (dialogIp.isNotBlank()) {
                                dialogIp = dialogIp.trim()
                                DeviceIpStorage.saveDeviceIp(context,dialogIp)
                                Toast.makeText(context, "Saved IP: $dialogIp", Toast.LENGTH_SHORT).show()
                                showDialog = false
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            IconButton(onClick = {
                onAction(DoorAction.CALL)
            }) {
                Icon(Icons.Default.Call, contentDescription = "Settings")
            }
            IconButton(onClick = {
                onAction(DoorAction.LOCK)
            }) {
                Icon(Icons.Default.LockOpen, contentDescription = "Settings")
            }


            if (user.role == UserRole.ADMIN) {
                IconButton(onClick = {
                    onAction(DoorAction.DEVICES)
                }) {
                    Icon(Icons.Default.Devices, contentDescription = "Settings")
                }
                IconButton(onClick = {
                        showDialog = true
                }) {
                    Icon(Icons.Default.PictureInPicture, contentDescription = "Settings")
                }
                IconButton(onClick = {
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

            if (user.role != null) {
                IconButton(onClick = {
                    onAction(DoorAction.LOGOUT)
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Settings")
                }
            } else {
                IconButton(onClick = {
                    onAction(DoorAction.LOGIN)
                }) {
                    Icon(Icons.Default.Person, contentDescription = "Settings")
                }
            }
        }
    )
}
