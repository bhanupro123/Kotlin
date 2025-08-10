package com.krishnaZyala.faceRecognition.ui.screen.multiServer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.krishnaZyala.faceRecognition.ui.screen.multiServer.DeviceInfo
import com.krishnaZyala.faceRecognition.ui.screen.multiServer.MultiServerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiServerScreen(
    viewModel: MultiServerViewModel = viewModel()
) {
    val devices by viewModel.devices.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Own Server Devices") })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(devices) { device ->
                DeviceCard(device, onConnect = { viewModel.connectToDevice(device.id) })
            }
        }
    }
}

@Composable
fun DeviceCard(device: DeviceInfo, onConnect: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Name: ${device.name}")
            Text(text = "IP: ${device.ip}")
            Text(text = "ID: ${device.id}")
            Button(
                onClick = onConnect,
                enabled = !device.isConnected,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (device.isConnected) "Connected" else "Connect")
            }
        }
    }
}
