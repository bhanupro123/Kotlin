package com.bhanupro.faceRecognition.ui.screen.ownServer

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanupro.faceRecognition.ui.navigation.LocalGlobalViewModel
import com.bhanupro.faceRecognition.ui.screen.ownServer.newDevice.ConnectionState
import com.bhanupro.faceRecognition.ui.screen.ownServer.newDevice.DeviceCardView

@Composable
fun OwnServerScreen() {

    val context = LocalContext.current
    val globalViewModel = LocalGlobalViewModel.current
    val masterController = globalViewModel.masterController

    val application = context.applicationContext as Application
    // These will only trigger recomposition when their values change
    val signalConnected by  globalViewModel.signalConnected.collectAsState()
    val videoSignal by  globalViewModel.videoConnected.collectAsState()
    val audioSignal by  globalViewModel.audioConnected.collectAsState()
    val devices by  globalViewModel.devices.collectAsState()

    // Convert to map for quick lookup without rebuilding UI each time
    val deviceMap by remember(devices) {
        mutableStateOf(devices.associateBy { it.ip })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status row (minimal recomposition)
        ConnectionStatus(signalConnected)

        AVConnectionStatus(connectionState = videoSignal, type = "Video")
        AVConnectionStatus(connectionState = audioSignal, type = "Audio")
        Spacer(Modifier.height(12.dp))

        // Device list
        deviceMap.forEach { (_, device) ->
            key(device.ip) { // Prevents recomposition of unchanged device cards
                DeviceCardView(
                    device = device,
                    application = application,
                    masterController
                )
            }
        }
    }
}
@Composable
private fun AVConnectionStatus(
    connectionState: ConnectionState,
    type: String // "Video" or "Audio"
) {
    when (connectionState) {
        ConnectionState.CONNECTING -> {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xAA000000)), // semi-transparent overlay
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("$type Connecting...", color = Color.White)
                }
            }
        }

        ConnectionState.CONNECTED -> {
            // Optional: show "Connected" message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x6600FF00)),
                contentAlignment = Alignment.Center
            ) {
                Text("$type Connected", color = Color.White)
            }
        }

        ConnectionState.AVAILABLE -> {
            // Optional: show "Failed" message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x66FF0000)),
                contentAlignment = Alignment.Center
            ) {
                Text("$type Connection Failed", color = Color.White)
            }
        }



    }
}


@Composable
private fun ConnectionStatus(connected: Boolean) {
    val statusText = if (connected) "Signal Server: Connected" else "Signal Server: Disconnected"
    val color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}
