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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smart.home.Category
import com.smart.home.Device
import com.smart.home.Devices.FanControlUIMulti
import com.smart.home.Devices.TVControlUI
import com.smart.home.RoundedIconButtonRow
import com.smart.home.SharedViewModel
import com.smart.home.Utils.DeviceType
import com.smart.home.Utils.StringConstants
import com.smart.home.Utils.iconMap




@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CategoryViewer(navController: NavController, sharedViewModel: SharedViewModel,category: Category=Category())
{
    val globalData=sharedViewModel.globalviewmodal?.value?.globalViewModelData?.value
    var selectedCategory by remember { mutableStateOf(if(category.devices.size>0) category.devices[0] else Device()) }

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
                navController.navigate(
                    "${StringConstants.PAIRDEVICE}/{category}" //Just modify your route accordingly
                        .replace(
                            oldValue = "{category}",
                            newValue =   GsonBuilder().create().toJson(category)
                        )
                )

            })
            {
                Icon( Icons.Rounded.Devices, contentDescription = "Pair Device")
            }
        }

        Row(modifier = Modifier.fillMaxSize().fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)){
            if (category.devices.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp))
                {
                    Text(text = "Devices:", fontSize = 16.sp, color = Color.Gray)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Set to 2 columns
                        modifier = Modifier
                            .fillMaxSize().fillMaxWidth().fillMaxHeight() ,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(category.devices) { device ->
                            DeviceItem(device,selectedCategory, clickabled = {
                                 if(selectedCategory.id!=device.id)
                                 {
                                     selectedCategory=device
                                 }
                            })
                        }
                    }
                }
            } else {
                Text(text = "No devices available", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            }
            Column (modifier = Modifier.weight(2f).fillMaxHeight())
            {
                 if(selectedCategory.type==DeviceType.FAN)
                 {
                     FanControlUIMulti()
                 }
              else  if(selectedCategory.type==DeviceType.TV)
                {
                    TVControlUI()
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
fun DeviceItem(device: Device,selectedCategory: Device,clickabled: () -> Unit) {
    Box(
        modifier = Modifier.clickable { clickabled() }
            .fillMaxWidth().padding(10.dp).clip(RoundedCornerShape(10.dp)).background(if(selectedCategory.id==device.id) Color.Blue else Color.Gray)

    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)){
            Text(text = device.name)
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Add Device")
                Column(modifier = Modifier.weight(1f))
                {
                    Text(text = "Type: ${device.type}", )
                }
            }
        }
    }
}
