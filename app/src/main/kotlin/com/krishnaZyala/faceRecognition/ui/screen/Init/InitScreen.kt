package com.krishnaZyala.faceRecognition.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.krishnaZyala.faceRecognition.data.model.AppState

@Composable
fun InitScreen(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Home Screen", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            appState.host.navigate("home") {
                popUpTo("splash"
                ) { inclusive = true }
            }
        }) {
            Text("Go to Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            appState.host.navigate("audioStream") {
                popUpTo("splash"
                ) { inclusive = true }
            }
        }) {
            Text("Go to Audio Stream")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            appState.host.navigate("media") {
                popUpTo("splash"
                ) { inclusive = true }
            }
        }) {
            Text("Go to Media Stream")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            appState.host.navigate("stream") {
                popUpTo("splash"
                ) { inclusive = true }
            }
        }) {
            Text("Go to Stream")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            appState.host.navigate("ownserver") {
                popUpTo("splash"
                ) { inclusive = true }
            }
        }) {
            Text("Server")
        }

    }


}

