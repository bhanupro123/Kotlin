package com.smart.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import com.smart.home.WebSocket.WebSocketViewModel

@Composable
fun WebSocketScreen(viewModel: WebSocketViewModel = viewModel()) {
    var message by remember { mutableStateOf("") }

    Column {
        Button(onClick = { viewModel.sendMessage("Message from Android") }) {
            Text("Send Message")
        }
        Text(text = "Received: $message")
    }
}

@Composable
fun MyApp() {
    val viewModel: WebSocketViewModel = viewModel()

    // Start WebSocket when app starts
    LaunchedEffect(Unit) {
        viewModel.startWebSocket("ws://your-websocket-url")
    }

    WebSocketScreen(viewModel = viewModel)
}
