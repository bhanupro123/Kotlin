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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.SharedViewModel
import com.smart.home.Utils.DeviceType
import com.smart.home.Utils.StringConstants
import WebSocketService
import androidx.compose.material3.Button
import com.google.gson.GsonBuilder
import com.smart.home.DeviceMetaInfo
import com.smart.home.PairDevice
import com.smart.home.ScheduleMetaData
import com.smart.home.SensorMetaData
import com.smart.home.SnackbarManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDevices(webSocketClient : WebSocketService,
                navController: NavController,
                sharedViewModel: SharedViewModel,
                category: Category,inside:Boolean=false) {

    val globalData= sharedViewModel.globalViewModelData.value
    var deviceConfig by remember { mutableStateOf(DeviceMetaInfo()) }
    var expanded by remember { mutableStateOf(false) }
    var icon by remember { mutableStateOf("Favorite") }
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
            if(!inside)
                Box(modifier = Modifier.clickable {

                    navController.popBackStack()
                })
                {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Add Device")
                }

            Text(modifier = Modifier.weight(1f), text = "Pair Device", fontSize = 20.sp)
            Box(modifier = Modifier.clickable {

                if(deviceConfig.name.length<2)
                    return@clickable
                if(deviceConfig.destination.length<2)
                    return@clickable
                if(deviceConfig.type.isEmpty())
                    return@clickable
                webSocketClient.sendMessage("AddDevice"+GsonBuilder().create().toJson(deviceConfig.copy(roomId = category.id)).toString())
                if(!inside)
                    navController.popBackStack()
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
                            expanded=true
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
                        text = (if(deviceConfig.type.isEmpty())  "Select Device Type"  else deviceConfig.type),
                        color = Color.White, fontSize = 16.sp)

                    Icon(tint = Color.White, modifier = Modifier.padding(horizontal = 10.dp),
                        imageVector =  Icons.Filled.ExpandCircleDown,
                        contentDescription = "")
                    if (expanded) {
                        DropdownMenu(
                            expanded = true,
                            modifier = Modifier
                                .fillMaxWidth(0.665f)
                                .background(Color.White).wrapContentHeight() ,
                            onDismissRequest = { expanded = false }) {

                            globalData?.devicesCat?.forEach { option ->
                                DropdownMenuItem(onClick = {

                                } // Reset search query
                                    ,text={
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.clickable {
                                                deviceConfig.type=option
                                                expanded=false
                                            }
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp))
                                        {
                                            Text(modifier = Modifier.fillMaxWidth(), text = option, color = Color.Black, fontSize = 16.sp)

                                        }
                                    })
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

                IconGrid(selectedIcon = icon, onIconSelected = {
                    deviceConfig.icon=it
                    deviceConfig.icon=it
                    icon=it
                })
                Spacer(modifier = Modifier.height(20.dp))

            }
            if(deviceConfig.type.isNotEmpty() )
            {
                if(deviceConfig.type.lowercase()==DeviceType.FAN.lowercase())
                {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Button(
                            onClick = {
                                SnackbarManager.showSnackbar("schedule_"+category.id+"_"+deviceConfig.name)
                                println( GsonBuilder().create().toJson("schedule_"))

                                val schedule = ScheduleMetaData(
                                    deviceMetaInfo = DeviceMetaInfo(),
                                    enabled = 1,
                                    endTime = "06:00",
                                    tableName = "schedule_"+category.id+"_"+deviceConfig.name,
                                    name = "111",
                                    priority = 1,
                                    sensors = listOf(
                                        SensorMetaData(
                                            enabled = 1,
                                            id = "",
                                            name = "Sence",
                                            priority = 1,
                                            relayon = "",
                                            sensorName = "PIR",
                                            triggerAt = 1,
                                            type = "Digital",
                                            value = 1
                                        )
                                    ),
                                    startTime = "22:00"
                                )
                                webSocketClient.sendMessage( "schedule_"+Json.encodeToString(listOf(schedule)))

                            },
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                        ) {
                            Text(text = "Save 1")
                        }
//                        DeviceConfiguration(deviceMetaInfo=deviceConfig,
//                            onDeviceChange = { updatedDevice ->
//                                deviceConfig = updatedDevice // Update the state
//                            },
//                            onDone = {
//                                SnackbarManager.showSnackbar("schedule_"+category.id+"_"+deviceConfig.name)
//                                println( GsonBuilder().create().toJson("schedule_"))
//
//                                val schedule = ScheduleMetaData(
//                                    deviceMetaInfo = DeviceMetaInfo(
//
//                                    ),
//                                    enabled = 1,
//                                    endTime = "06:00",
//                                    id = "schedule_"+category.id+"_"+deviceConfig.name,
//                                    name = "111",
//                                    priority = 1,
//                                    sensors = listOf(
//                                        SensorMetaData(
//                                            enabled = 1,
//                                            id = "",
//                                            name = "Sence",
//                                            priority = 1,
//                                            relayon = "",
//                                            sensorName = "PIR",
//                                            triggerAt = 1,
//                                            type = "Digital",
//                                            value = 1
//                                        )
//                                    ),
//                                    startTime = "22:00"
//                                )
//
//
//                                webSocketClient.sendMessage( "schedule_"+Json.encodeToString(listOf(schedule)))
//
//                            },false)

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

        }
    }
}

