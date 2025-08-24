package com.bhanupro.faceRecognition.ui.screen.dashBoard

import DoorControlTopBar
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
import androidx.compose.ui.platform.LocalContext
import com.bhanupro.faceRecognition.app.SnackbarManager
import com.bhanupro.faceRecognition.ui.global.UserData
import com.bhanupro.faceRecognition.ui.global.UserRole
import com.bhanupro.faceRecognition.ui.navigation.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorControlDashboard(state: AppState) {
    val globalViewModel = LocalGlobalViewModel.current
    val user by globalViewModel.userData.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    // Dialog states
    var showLoginDialog by remember { mutableStateOf(false) }
    var isItForUnLock by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }
    fun unlockDoor()
    {
        SnackbarManager.showMessage("Unlocked")
    }
    LaunchedEffect (Unit) {
        globalViewModel.startUsbAutoConnect()

            withContext(Dispatchers.Main) {
              globalViewModel.fetchPiIp()
            }
        }
    val items = listOf(
        DashboardItem("Call", Icons.Default.Call) {
            state.host.navigate(NavRoutes.OWN_SERVER)
                                                  },
        DashboardItem("Unlock Door", Icons.Default.LockOpen) {
            when (user.role) {
                UserRole.ADMIN -> {  unlockDoor() }
                UserRole.MEMBER -> {   unlockDoor()  }
                UserRole.GUEST -> {   unlockDoor()  }
                null ->{
                    isItForUnLock=true
                    showLoginDialog=true
                }
            }
        },
        if (user.role!=null) {
            // Logout action
            DashboardItem("Logout", Icons.Default.ExitToApp) {
                globalViewModel.setUser(UserData()) // Reset to default user
                SnackbarManager.showMessage("Logged out")
            }
        } else {

            DashboardItem("Login", Icons.Default.Person) {
                isItForUnLock=false
                showLoginDialog = true
            }
        }
   )


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            DoorControlTopBar()
          if(user.role!=null)
              UserCard(user,globalViewModel, modifier = Modifier.padding(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.title }) { item ->
                    ActionCard(item)
                }
            }
        }
    }

    // Dialogs
    if (showLoginDialog) {
        MultiMethodDialog(
            title = "Login",
            methods = listOf(
                MethodOption(Icons.Default.Face, "Face Recognition") {
                    state.host.navigate(NavRoutes.RECOGNISE)
                },
                MethodOption(Icons.Default.Password, "PIN") {
                    showPinDialog=true
                },
                MethodOption(Icons.Default.Grid4x4, "Pattern") {

                }
            ),
            onDismiss = { showLoginDialog = false }
        )
    }

     if (showPinDialog) {
        PinDialog(
            context = LocalContext.current,
            onSuccess = {
                if(isItForUnLock)
                {
                    unlockDoor()
                }
                else{
                    SnackbarManager.showMessage("PIN login successful")
                    globalViewModel.setUser(UserData(role = UserRole.ADMIN,
                        username = "Bhanu",
                        id = "Bhanu", loginMode = "PIN"))
                }
             },
            onDismiss = { showPinDialog = false }
        )
    }
    if (showCallDialog) {
        AlertDialog(
            onDismissRequest = { showCallDialog = false },
            title = { Text("Call") },
            text = { Text("Initiate a call to the connected device.") },
            confirmButton = {
                TextButton(onClick = { showCallDialog = false }) { Text("OK") }
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
                    ).padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
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
                            method.onClick() },
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


