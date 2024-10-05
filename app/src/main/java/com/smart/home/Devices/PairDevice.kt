package com.smart.home.Devices

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.GsonBuilder
import com.smart.home.Category
import com.smart.home.Device
import com.smart.home.SharedViewModel
import com.smart.home.SnackbarManager
import com.smart.home.Utils.DeviceType
import com.smart.home.Utils.StringConstants
import com.smart.home.Utils.iconMap
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDevice(navController: NavController, sharedViewModel: SharedViewModel, category: Category) {

    val globalData= sharedViewModel.globalviewmodal.value.globalViewModelData.value
    var deviceConfig by remember { mutableStateOf(com.smart.home.PairDevice()) }
    var expanded by remember { mutableStateOf(false) }
    var icon by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Add Device")
            }

            Text(modifier = Modifier.weight(1f), text = "Pair Device", fontSize = 20.sp)
            Box(modifier = Modifier.clickable {


            })
            {
                Icon( Icons.Filled.Done, contentDescription = "Add Device")
            }
        }
       Row(modifier = Modifier.weight(1f).fillMaxSize(),
           horizontalArrangement = Arrangement.spacedBy(10.dp)
       )
       {
           Column(   modifier = Modifier
               .fillMaxHeight().weight(1f)
               .clip(RoundedCornerShape(10.dp)).background(Color.DarkGray)
               .padding( horizontal = 16.dp),) {
               OutlinedTextField(
                   value = deviceConfig.id,
                   onValueChange = {   deviceConfig=(deviceConfig.copy(id = it)) },
                   label = { Text("Device ID") },
                   modifier = Modifier.fillMaxWidth(),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(10.dp))
               OutlinedTextField(
                   value = deviceConfig.name,
                   onValueChange = {
                       deviceConfig=(deviceConfig.copy(name = it))
                      },
                   label = { Text("Device Name") },
                   modifier = Modifier.fillMaxWidth(),
                   singleLine = true
               )
               Spacer(modifier = Modifier.height(10.dp))

               Row(
                   modifier = Modifier
                       .fillMaxWidth().clickable {
                          if(globalData?.device?.size==0) navController.navigate(StringConstants.ADDDEVICE)
                           else  expanded=true
                       }
                       .border(
                           width = 1.dp, // Border width
                           color = Color.White, // Border color
                           shape = RoundedCornerShape(6.dp) // Corner radius
                       )
                       .height(55.dp)
                       .background(color = Color.Transparent) ,
                   verticalAlignment = Alignment.CenterVertically,
               ) {
                   Text(modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                       text = if(deviceConfig.deviceType.deviceName.isNotEmpty()) deviceConfig.deviceType.deviceName
                       else "Select Device Type",
                       color = Color.White, fontSize = 16.sp)
                   Icon(tint = Color.White, modifier = Modifier.padding(horizontal = 10.dp), imageVector =  Icons.Filled.ExpandCircleDown, contentDescription = "Add Device")
                   if (expanded) {
                       DropdownMenu(
                           expanded = true,
                           modifier = Modifier
                               .fillMaxWidth(0.665f)
                               .background(Color.White).wrapContentHeight() ,
                           onDismissRequest = { expanded = false }) {
                           globalData?.device?.forEach { option ->
                               DropdownMenuItem(onClick = {

                               } // Reset search query
                                   ,text={
                                       Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                           modifier = Modifier.clickable {
                                               deviceConfig.deviceType=option
                                               expanded=false
                                           }
                                               .fillMaxWidth()
                                               .padding(vertical = 10.dp))
                                       {
                                           Text(modifier = Modifier.fillMaxWidth(), text = option.deviceName, color = Color.Black, fontSize = 16.sp)

                                       }
                                   })
                           }
                       }
                   }
               }


               Spacer(modifier = Modifier.height(24.dp))

               IconGrid(selectedIcon = icon, onIconSelected = {
                   deviceConfig.icon=it
                   icon=it
               })
               Spacer(modifier = Modifier.height(20.dp))

           }
           if(deviceConfig.deviceType.deviceName.isNotEmpty())
           {
                   if(deviceConfig.deviceType.deviceName.lowercase()==DeviceType.FAN.lowercase())
                   {
                       Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                           DeviceConfiguration(deviceConfig=deviceConfig,
                               onDeviceChange = { updatedDevice ->
                                   deviceConfig = updatedDevice // Update the state
                               })
                       }
                   }
           }

       }
    }
}

