package com.krishnaZyala.faceRecognition.ui.screen.dashBoard

import UserCard
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krishnaZyala.faceRecognition.data.model.AppState
import com.krishnaZyala.faceRecognition.ui.navigation.LocalGlobalViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import com.krishnaZyala.faceRecognition.app.SnackbarManager
import com.krishnaZyala.faceRecognition.ui.global.UserRole
import com.krishnaZyala.faceRecognition.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlDashboard(state: AppState) {
    val scope = rememberCoroutineScope()
    val showChangeLogin = remember { mutableStateOf(false) }
    val showPincodeDialog = remember { mutableStateOf(false) }
    val showFaceUnlockDialog = remember { mutableStateOf(false) }
    val pincode = remember { mutableStateOf("") }
    val globalViewModel = LocalGlobalViewModel.current
    val user by globalViewModel.userData.collectAsState()

    LaunchedEffect(Unit) {
       // globalViewModel.resetTimeout()
    }

    // Stable click lambdas
    val onFaceClick = remember { { showFaceUnlockDialog.value = true } }
    val onPinClick = remember { { showPincodeDialog.value = true } }
    val onCallClick = remember { { /* TODO */ } }
    val onMoreOptionsClick = remember { { /* TODO */ } }
    val onChangeUserClick = remember { { showChangeLogin.value = true } }

    // Stable dashboard items
    val items = remember {
        listOf(
            DashboardItem("Unlock by Face", Icons.Default.Face, onFaceClick),
            DashboardItem("PIN", Icons.Filled.Password, onPinClick),
            DashboardItem("Call", Icons.Default.Call, onCallClick),
            DashboardItem("More Options", Icons.Default.Settings, onMoreOptionsClick),
            DashboardItem("Change User", Icons.Default.Person, onChangeUserClick)
        )
    }

    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) {
        DeviceControlsModal(onDismiss = { showSettings = false })
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Door Control", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )

            UserCard(user, modifier = Modifier.padding(12.dp))
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = items,
                    key = { it.title }
                ) { item ->
                    ActionCard(item)
                }
            }
        }
    }

    if (showPincodeDialog.value) {
        PincodeDialog(
            pincode = pincode.value,
            onValueChange = { pincode.value = it },
            onConfirm = {
                showPincodeDialog.value = false
                scope.launch {
                    // Handle PIN confirmation
                }
            },
            onDismiss = { showPincodeDialog.value = false }
        )
    }

    if (showFaceUnlockDialog.value) {
        FaceUnlockDialog(
            onAddFace = {
                showFaceUnlockDialog.value = false
                if(globalViewModel.userData.value.role==UserRole.ADMIN)
                {
                    state.host.navigate(NavRoutes.HOME)
                }
                else{
                    SnackbarManager.showMessage("Please login as Admin")
                }
            },
            onRecognize = {
                showFaceUnlockDialog.value = false
                state.host.navigate(NavRoutes.RECOGNISE)
            },
            onListFaces = {
                showFaceUnlockDialog.value = false
                if(globalViewModel.userData.value.role==UserRole.ADMIN)
                {
                    state.host.navigate(NavRoutes.HOME)
                }
                else{
                    SnackbarManager.showMessage("Please login as Admin")
                }
            },
            onDismiss = { showFaceUnlockDialog.value = false }
        )
    }

    if (showChangeLogin.value) {
        ChangeUserDialog(
            onConfirm = { showChangeLogin.value = false },
            onDismiss = { showChangeLogin.value = false }
        )
    }
}

@Immutable
private data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun ActionCard(item: DashboardItem) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { item.onClick() }
                )
            }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun FaceUnlockDialog(
    onAddFace: () -> Unit,
    onRecognize: () -> Unit,
    onListFaces: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Face Unlock Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FaceDialogOption(Icons.Default.Face, "Add Face", onAddFace)
                FaceDialogOption(Icons.Default.Search, "Recognize", onRecognize)
                FaceDialogOption(Icons.Default.List, "List of Faces", onListFaces)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun FaceDialogOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PincodeDialog(
    pincode: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Enter Security PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = pincode,
                    onValueChange = onValueChange,
                    label = { Text("PIN Code") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Face Unlock",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Use Face Unlock",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pincode) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Unlock")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ChangeUserDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change User / Login") },
        text = {
            Text("Open the user management screen to switch accounts or register a new face/pincode.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Open") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
