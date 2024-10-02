package com.smart.home.WebSocket.Category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Power
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.Device
import com.smart.home.RoundedIconButtonRow
import com.smart.home.SharedViewModel
import com.smart.home.Utils.StringConstants
import com.smart.home.Utils.iconMap


@Composable
fun DeviceItem(device: Device) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = device.name, fontSize = 16.sp)
        // Placeholder for device actions, e.g., toggle on/off
        IconButton(onClick = { /* Handle device action */ }) {
            Icon(Icons.Filled.Power, contentDescription = "Power")
        }
    }
}
@Composable
fun CategoryViewer(navController: NavController, sharedViewModel: SharedViewModel,category: Category=Category())
{
    val globalData=sharedViewModel.globalviewmodal?.value?.globalViewModelData?.value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle category click */ }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category.name, fontSize = 20.sp)
            IconButton(onClick = { /* Handle add device action */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Device")
            }
        }

        // Display devices in the category
        if (category.devices.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = "Devices:", fontSize = 16.sp, color = Color.Gray)
                LazyColumn {
                    items(category.devices) { device ->
                        DeviceItem(device)
                    }
                }
            }
        } else {
            Text(text = "No devices available", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconGrid(selectedIcon: ImageVector, onIconSelected: (ImageVector) -> Unit) {
    FlowRow (){
        iconMap.keys.forEach{ icon ->
            IconButton(onClick = { iconMap[icon]?.let { onIconSelected(it) } }) {
                iconMap[icon]?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (it == selectedIcon) Color.Blue else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
