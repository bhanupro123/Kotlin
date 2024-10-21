package com.smart.home.WebSocket.Category

import android.annotation.SuppressLint
import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.SharedViewModel
import com.smart.home.Utils.DeviceType
import com.smart.home.Utils.iconMap
import WebSocketService
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.smart.home.DeviceMetaInfo
import com.smart.home.Devices.AddDevice
import com.smart.home.Devices.DeviceConfiguration
import com.smart.home.ScheduleMetaData
import com.smart.home.SensorMetaData
import com.smart.home.SnackbarManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CategoryViewer(webSocketClient : WebSocketService, navController: NavController,
                   sharedViewModel: SharedViewModel, category: Category=Category())
{
    val globalData= sharedViewModel.globalViewModelData.collectAsState()
    val cat =globalData.value.categories.find { it.id==category.id }
    var deviceMetaInfo by remember { mutableStateOf(DeviceMetaInfo()) }
    var mode by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd)
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

                Text(modifier = Modifier.weight(1f), text = category.name, fontSize = 20.sp)
                Box(modifier = Modifier.clickable {

                    mode="create"
                    deviceMetaInfo=DeviceMetaInfo()
                })
                {
                    Icon( Icons.Rounded.Devices, contentDescription = "Pair Device")
                }
            }

            Row(modifier = Modifier.fillMaxSize().fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)){
                if (cat?.devices?.isNotEmpty() == true) {
                    Column(modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp))
                    {
                        Text(text = "Devices:", fontSize = 16.sp, color = Color.Gray)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2), // Set to 2 columns
                            modifier = Modifier
                                .fillMaxSize().fillMaxWidth().fillMaxHeight() ,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(cat.devices) { device ->
                                DeviceItem(device,deviceMetaInfo, clickabled = {
                                    deviceMetaInfo=device
                                    mode=""
                                })
                            }
                        }
                    }
                }

                Column (modifier = Modifier.weight(2f).fillMaxHeight())
                {
                    val tableName="schedule_"+category.id.replace(" ","_")+"_"+category.name.replace(" ","_")
                    if(deviceMetaInfo.type==DeviceType.FAN&& cat?.devices?.isNotEmpty() == true)
                    {
                        Text(tableName)
                        DeviceConfiguration( category=category,
                            webSocketClient=webSocketClient,
                          deviceMetaInfo = deviceMetaInfo,
                            onDeviceChange = {
                            },
                            onDone = {
                                SnackbarManager.showSnackbar(it.toString())
//                                val schedule = ScheduleMetaData(
//                                    deviceMetaInfo = DeviceMetaInfo(),
//                                    enabled = 1,
//                                    endTime = "06:00",
//                                    id = tableName,
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
                              // webSocketClient.sendMessage( "insertschedule_"+ GsonBuilder().create().toJson(listOf(schedule)))

                            }, isItNew = false
                        )
                    }
                    else if(mode=="create"||cat?.devices?.isEmpty() == true)
                    {
                        AddDevice(webSocketClient,navController,sharedViewModel,category,true)
                    }

                }
            }
        }

//    FloatingActionButton(
//        onClick = {  },
//        modifier = Modifier.offset(x= (-20).dp,y=(-20).dp)
//            .shadow(8.dp, RoundedCornerShape(32.dp))
//            .size(56.dp).background (Color.White)
//    ) {
//        Icon(
//            imageVector = Icons.Default.Add,
//            contentDescription = "Lights Icon",
//            tint = Color.White, // Customize the tint color as needed
//            modifier = Modifier.size(48.dp) // Adjust icon size
//        )
//    }
    }
}

@Composable
fun DeviceItem(device: DeviceMetaInfo, selectedCategory: DeviceMetaInfo, clickabled: () -> Unit) {
    Box(
        modifier = Modifier.clickable { clickabled() }
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if(selectedCategory.id==device.id) Color.Blue else Color.Gray)

    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)){
            Text(text = device.name!!)
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                iconMap[device.icon]?.let { Icon(imageVector = it, contentDescription = "Add Device") }
                Column(modifier = Modifier.weight(1f))
                {
                    Text(text = "Type: ${device.destination}", )
                }
            }
        }
    }
}
