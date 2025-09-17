package com.bhanupro.faceRecognition.ui.screen.dashBoard

import DoorControlTopBar
import UserCard
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhanupro.faceRecognition.data.model.AppState
import com.bhanupro.faceRecognition.ui.navigation.LocalGlobalViewModel
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bhanupro.faceRecognition.app.SnackbarManager
import com.bhanupro.faceRecognition.ui.global.UserData
import com.bhanupro.faceRecognition.ui.global.UserRole
import com.bhanupro.faceRecognition.ui.navigation.NavRoutes
import com.bhanupro.faceRecognition.ui.screen.ownServer.OwnServerScreen
import com.bhanupro.faceRecognition.ui.screen.ownServer.getWifiIpAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanupro.faceRecognition.ui.global.DashboardViewModel
import com.bhanupro.faceRecognition.ui.global.Device
import org.json.JSONObject

fun iconForDeviceType(type: String): ImageVector {
    return when (type) {
        "AC Light", "PWM", "Light" -> Icons.Default.Lightbulb
        "AC Fan" -> Icons.Filled.AcUnit
        "AC Motor", "Tank" -> Icons.Default.Water
        "Tap" -> Icons.Default.WaterDrop
        "Music" -> Icons.Default.MusicNote
        else -> Icons.Default.Devices // fallback
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceCard(
    device: Device,
    onToggle: (Device) -> Unit,
    onSave: (Device) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    val backgroundColor = if ( device.status) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (device.status) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    // --- Device Card ---
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .combinedClickable(
                onClick = { onToggle(device) },
                onLongClick = { showSettings = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (device.deviceType.contains("PWM", ignoreCase = true)) {
                    val progress = (device.brightness / 100f).coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(48.dp), // slightly bigger than icon
                        strokeWidth = 4.dp,
                        trackColor = contentColor.copy(alpha = 0.4f),
                        color =  FloatingActionButtonDefaults.containerColor
                    )
                }
                val infiniteTransition = rememberInfiniteTransition(label = "fanRotation")
                // Map device speed (1-5) to rotation duration (ms)
                val rotationDuration = when (device.brightness.coerceIn(1, 5)) {
                    1 -> 3000  // slowest
                    2 -> 2500
                    3 -> 2000
                    4 -> 1500
                    5 -> 1000  // fastest
                    else -> 2000
                }
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(rotationDuration, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "fanRotationAnim"
                )

                val rotationModifier = if (
                    device.deviceType.contains("Fan", ignoreCase = true) && device.status
                ) {
                    Modifier.rotate(rotation)
                } else {
                    Modifier // no rotation
                }
                Icon(
                    imageVector = iconForDeviceType(device.deviceType),
                    contentDescription = device.name,
                    modifier = Modifier.size(32.dp).then(rotationModifier),
                    tint = contentColor
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                maxLines = 1
            )
        }
    }

    // --- Settings Modal ---
    if (showSettings) {
        // keep temp state for controls
        var tempBrightness by remember { mutableStateOf(device.brightness) }
        var tempTimeout by remember { mutableStateOf(device.timeout.toInt()) }

        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = {
                    val updated = device.copy(
                        brightness = tempBrightness,
                        timeout = tempTimeout.toString(),
                        mode ="user"
                    )
                    onSave(updated) // send updated device
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close") }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = iconForDeviceType(device.deviceType),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(device.name, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Brightness / PWM Control
                    if (device.deviceType.contains("PWM", true)

                    ) {
                        Text("Brightness / Speed: $tempBrightness%")
                        Slider(
                            value = tempBrightness.toFloat(),
                            onValueChange = { tempBrightness = it.toInt() },
                            valueRange = 0f..100f
                        )
                    }

                    // Timeout Control
                    Text("Timeout: $tempTimeout sec")
                    Slider(
                        value = tempTimeout.toFloat(),
                        onValueChange = { tempTimeout = it.toInt() },
                        valueRange = 0f..600f,
                        steps = 20
                    )

                    // Mode Selector
                    Text("Mode")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                                onClick = {  },
                                label = { Text(device.mode) },
                                leadingIcon = {
                                    Icon(Icons.Default.Check, null)
                                }
                            )
                    }

                    Text("Last Trigger: ${device.lastTriggerAt}", style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlDashboard(
    state: AppState,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val globalViewModel = LocalGlobalViewModel.current
    val user by globalViewModel.userData.collectAsState()
    val signalConnected by globalViewModel.signalConnected.collectAsState()
    val devices by globalViewModel.devicesList.collectAsState()
    val uiState by dashboardViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun onCall() {
        state.host.navigate(NavRoutes.OWN_SERVER)
    }

    fun onLogin() {
        dashboardViewModel.setShowLoginDialog(true, unlock = false)
    }


    DisposableEffect(Unit) {
        onDispose { globalViewModel.stopAll() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column() {
                DoorControlTopBar(
                    onAction = { action ->
                        when (action) {
                            DoorAction.LOCK -> {
                                when (user.role) {
                                    UserRole.ADMIN, UserRole.MEMBER, UserRole.GUEST ->
                                        dashboardViewModel.unlockDoor()
                                    null -> dashboardViewModel.setShowLoginDialog(true, unlock = true)
                                }
                            }
                            DoorAction.CALL -> {
                            onCall()
                            }
                            DoorAction.LOGIN -> {
                            onLogin()
                            }
                            DoorAction.DEVICES -> {
                                if (globalViewModel.devicesList.value.isEmpty()) {
                                    val message = """{ "type": "request_devices" }"""
                                    globalViewModel.masterController.sendToServer(message)
                                }
                                dashboardViewModel.setShowDevicesList(true)
                            }
                            DoorAction.LOGOUT ->{
                                globalViewModel.setUser(UserData())
                                dashboardViewModel.setShowDevicesList(false)
                                SnackbarManager.showMessage("Logged out")
                            }
                        }
                    }
                )

                if (user.role != null)
                    UserCard(user, globalViewModel, modifier = Modifier.padding(12.dp))

//                Text("${globalViewModel.wsUrl.value}",modifier = Modifier.padding(vertical = 10.dp), style = MaterialTheme.typography.headlineSmall)
                if (!uiState.showDevicesList) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp) // spacing between buttons
                        ) {
                            Button(
                                onClick = {
                                    state.host.navigate(NavRoutes.OWN_SERVER)
                                },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text("Call")
                            }

                            Button(
                                onClick = {
                                    when (user.role) {
                                        UserRole.ADMIN, UserRole.MEMBER, UserRole.GUEST -> dashboardViewModel.unlockDoor()
                                        null -> dashboardViewModel.setShowLoginDialog(true, unlock = true)
                                    }
                                },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text("Lock / Unlock")
                            }

                            Button(
                                onClick = {
                               onLogin()
                                },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text("Login")
                            }
                        }
                    }

                } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp), // flexible grid like quick toggles
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(devices, key = { it.id }) { device ->
                        DeviceCard(device, onToggle = { updatedDevice ->
                            val json = JSONObject().apply {
                                put("id", updatedDevice.id)
                                put("mode", "user")
                                put("status", !updatedDevice.status) // or toggled
                            }
                            globalViewModel.masterController.sendToServer(json.toString())
                        },){  updatedDevice ->
                                val json = JSONObject()
                                    .put("id", updatedDevice.id)
                                    .put("brightness", updatedDevice.brightness)
                                    .put("timeout", updatedDevice.timeout)
                                    .put("mode", "user")
                                    .toString() // 👈 stringify
                                globalViewModel.masterController.sendToServer(json)
                        }
                    }

                }
                }
            }

            FloatingActionButton(
                onClick = { dashboardViewModel.setShowSheet(true) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                if (signalConnected) {
                    Icon(Icons.Default.SignalWifi4Bar, contentDescription = "Signal Connected")
                } else {
                    Icon(Icons.Default.SignalWifiOff, contentDescription = "No Signal")
                }
            }
        }
    }

    // ✅ Connection Dialog
    if (uiState.showConnectionDialog) {
        AlertDialog(
            onDismissRequest = { dashboardViewModel.setShowConnectionDialog(false) },
            title = { Text("Choose Connection") },
            text = { Text("Do you want to continue with local IP or WebSocket server?") },
            confirmButton = {
                TextButton(onClick = {
                    dashboardViewModel.setShowConnectionDialog(false)
                    coroutineScope.launch {
                        globalViewModel.startUsbAutoConnect()
                        globalViewModel.fetchPiIp()
                    }
                    SnackbarManager.showMessage("Selected Local IP")
                }) { Text("Local IP") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dashboardViewModel.setShowConnectionDialog(false)
                    globalViewModel.fetchWsUrl()
                    SnackbarManager.showMessage("Selected WS Server")
                }) { Text("WS Server") }
            }
        )
    }

    // ✅ Bottom Sheet
    if (uiState.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { dashboardViewModel.setShowSheet(false) },
            sheetState = sheetState
        ) {
            OwnServerScreen()
        }
    }

    // ✅ Login Dialog
    if (uiState.showLoginDialog) {
        MultiMethodDialog(
            title = "Login",
            methods = listOf(
                MethodOption(Icons.Default.Face, "Face Recognition") {
                    state.host.navigate(NavRoutes.RECOGNISE)
                },
                MethodOption(Icons.Default.Password, "PIN") {
                    dashboardViewModel.setShowPinDialog(true)
                },
                MethodOption(Icons.Default.Grid4x4, "Pattern") { }
            ),
            onDismiss = { dashboardViewModel.setShowLoginDialog(false) }
        )
    }

    // ✅ PIN Dialog
    if (uiState.showPinDialog) {
        PinDialog(
            context = LocalContext.current,
            onSuccess = {
                if (uiState.isItForUnLock) {
                    dashboardViewModel.unlockDoor()
                } else {
                    SnackbarManager.showMessage("PIN login successful")
                    globalViewModel.setUser(
                        UserData(
                            role = UserRole.ADMIN,
                            username = "Bhanu",
                            id = "Bhanu",
                            loginMode = "PIN"
                        )
                    )
                }
            },
            onDismiss = { dashboardViewModel.setShowPinDialog(false) }
        )
    }

    // ✅ Call Dialog
    if (uiState.showCallDialog) {
        AlertDialog(
            onDismissRequest = { dashboardViewModel.setShowCallDialog(false) },
            title = { Text("Call") },
            text = { Text("Initiate a call to the connected device.") },
            confirmButton = {
                TextButton(onClick = { dashboardViewModel.setShowCallDialog(false) }) { Text("OK") }
            }
        )
    }
}

@Immutable
private data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)


@Immutable
data class MethodOption(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun MultiMethodDialog(
    title: String,
    methods: List<MethodOption>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { method ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                                onDismiss()
                            method.onClick()
                                                                     },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(method.icon, contentDescription = method.label)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(method.label)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}


