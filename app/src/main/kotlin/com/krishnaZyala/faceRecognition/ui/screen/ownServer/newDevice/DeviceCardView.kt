package com.krishnaZyala.faceRecognition.ui.screen.ownServer.newDevice

import android.app.Application
import androidx.compose.foundation.Image
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.Device_Info
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.MasterController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.getDeviceName
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.getWifiIpAddress


@Composable
fun DeviceCardView(
    device: Device_Info,
    application: Application,
    masterController: MasterController
) {
    val context = LocalContext.current
    var showPopup by remember { mutableStateOf(false) }
    val viewModel: NewDeviceViewmodel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application)
    )

    fun connect() {
        if (viewModel.connectionState == ConnectionState.AVAILABLE) {
            val json = """
                {
                    "fromIp": "${getWifiIpAddress(context)}",
                    "name": "${getDeviceName()}",
                    "toIp":"${device.ip}",
                    "type":"broadcast",
                    "action":"wakeup"
                }
            """.trimIndent()
            masterController.sendToServer(json)
        }
        viewModel.startStreaming(device.ip)
        showPopup = true
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                connect()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(device.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("IP: ${device.ip}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(6.dp))
            when (viewModel.connectionState) {
                ConnectionState.CONNECTING -> {
                    Text("Status: Connecting...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                ConnectionState.CONNECTED -> {
                    Text("Status: Connected", color = Color.Green, style = MaterialTheme.typography.bodySmall)
                }

                else -> {
                    Text("Status: Idle", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showPopup) {
        LiveFeedPopup(viewModel,
            onRetry = {
                connect()
            }
        ) {
           // viewModel.exit()
            showPopup = false
        }
    }
}

@Composable
fun LiveFeedPopup(
    viewModel: NewDeviceViewmodel,
    onRetry:()->Unit,
    onClose: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = {
        viewModel.exit()
        onClose() }) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Black)
        ) {
            when (viewModel.connectionState) {
                ConnectionState.CONNECTING -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Connecting...", color = Color.White)
                    }
                }
                ConnectionState.CONNECTED -> {
                    val frame = viewModel.latestFrame
                    if (frame != null) {
                        Image(
                            bitmap = frame.asImageBitmap(),
                            contentDescription = "Live Stream",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                ConnectionState.AVAILABLE -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Failed to connect", color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            onRetry()
                             }) {
                            Text("Retry")
                        }
                    }
                }


            }

            // Hang up button (top-right)
            Button(
                onClick = {
                    viewModel.exit()
                    onClose()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Hang Up", color = Color.White)
            }
        }
    }
}

