package com.bhanupro.faceRecognition.ui.screen.home

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
import androidx.navigation.NavHostController
import com.bhanupro.faceRecognition.ui.navigation.NavRoutes

@Composable
fun InitScreen(navigation: NavHostController) {

    fun navigate(route:String)
    {
        navigation.navigate(route) {
            popUpTo(0) { inclusive = true }  // clears the whole back stack
        }
    }


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
            navigate(NavRoutes.HOME)
        }) {
            Text("Go to Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigate(NavRoutes.AUDIO_STREAM)
        }) {
            Text("Go to Audio Stream")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigate(NavRoutes.MEDIA)
        }) {
            Text("Go to Media Stream")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigate("stream")
        }) {
            Text("Go to Stream")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigate(NavRoutes.OWN_SERVER)
        }) {
            Text("Server")
        }


        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigate(NavRoutes.DASHBOARD)
        }) {
            Text("Dashboard")
        }

    }


}

