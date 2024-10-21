package com.smart.home.Devices

import WebSocketService
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.Device
import com.smart.home.SharedViewModel
import com.smart.home.Utils.iconMap

@Composable
fun ImportDevice(webSocketClient : WebSocketService, navController: NavController, sharedViewModel: SharedViewModel, category: Category) {

    var deviceId by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var deviceType by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.clickable {
                navController.popBackStack()
            })
            {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Pair Device")
            }

            Text(modifier = Modifier.weight(1f), text = "Pair Device", fontSize = 20.sp)
            Box(modifier = Modifier.clickable {


            })
            {
                Icon( Icons.Filled.Done, contentDescription = "Pair Device")
            }
        }
        OutlinedTextField(
            value = deviceId,
            onValueChange = { deviceId = it },
            label = { Text("Device ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("Device Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = deviceType,
            onValueChange = { deviceType = it },
            label = { Text("Device Type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        IconGrid() {

        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconGrid(selectedIcon: String="", onIconSelected: (String) -> Unit) {
    FlowRow (){
        iconMap.keys.forEach{ icon ->
            IconButton(onClick = { iconMap[icon]?.let { onIconSelected(icon) } }) {
                iconMap[icon]?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (icon == selectedIcon) Color.Green else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

