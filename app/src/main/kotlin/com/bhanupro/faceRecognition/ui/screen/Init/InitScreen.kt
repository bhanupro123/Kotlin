package com.bhanupro.faceRecognition.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhanupro.faceRecognition.lib.DeviceType
import com.bhanupro.faceRecognition.lib.DeviceTypeStorage
import com.bhanupro.faceRecognition.ui.navigation.LocalGlobalViewModel
import com.bhanupro.faceRecognition.ui.navigation.NavRoutes

@Composable
fun InitScreen(navigation: NavHostController) {
    val globalViewModel = LocalGlobalViewModel.current
   val context = LocalContext.current
    fun navigate(route:String)
    {
        navigation.navigate(route) {
            popUpTo(0) { inclusive = true }  // clears the whole back stack
        }
    }

    val savedType = DeviceTypeStorage.getDeviceType(context)
    var showDialog by remember { mutableStateOf(savedType == "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Home Screen", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navigate(NavRoutes.HOME) }) {
            Text("Go to Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // show AlertDialog if needed
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select Device Type") },
                text = {
                    Column {
                        DropdownMenuItem(
                            text = { Text(DeviceType.NORMAL.name) },
                            onClick = {
                                DeviceTypeStorage.saveDeviceType(context, DeviceType.NORMAL.name)
                                globalViewModel.setDeviceType(DeviceType.NORMAL)
                                showDialog = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(DeviceType.SECURITY_MAIN.name) },
                            onClick = {
                                DeviceTypeStorage.saveDeviceType(context, DeviceType.SECURITY_MAIN.name)
                                globalViewModel.setDeviceType(DeviceType.SECURITY_MAIN)
                                showDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navigate(NavRoutes.OWN_SERVER) }) {
            Text("Server")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navigate(NavRoutes.DASHBOARD) }) {
            Text("Dashboard")
        }
    }

}

