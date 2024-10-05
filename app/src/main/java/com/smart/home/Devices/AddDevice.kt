package com.smart.home.Devices

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.Device
import com.smart.home.DeviceType
import com.smart.home.MetaData
import com.smart.home.Range
import com.smart.home.SharedViewModel
import com.smart.home.SnackbarManager
import com.smart.home.Utils.iconMap


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AddDevice(navController: NavController, sharedViewModel: SharedViewModel,) {
    val globalData=sharedViewModel.globalviewmodal?.value?.globalViewModelData?.value
    var deviceName by remember { mutableStateOf("") }
    var isRGB by remember { mutableStateOf(false) }
    var rgbColor by remember { mutableStateOf("#f00") } // Default color
    var sliderValue by remember { mutableStateOf(0f) } // Slider value (0-255)
    var min by remember { mutableFloatStateOf(1f) } // Slider value (0-255)
    var max by remember { mutableStateOf(1f) } // Slider value (0-255)
    var step by remember { mutableStateOf(1f) } // Slider value (0-255)
    var value by remember { mutableStateOf(1f) } // Slider value (0-255)
    val context=LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize().verticalScroll(rememberScrollState())
        .padding(vertical = 16.dp, horizontal = 20.dp)) {
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "")
            }
            Text(modifier = Modifier.weight(1f), text = "Add Device", fontSize = 20.sp)
            Box(modifier = Modifier.clickable {
                if(deviceName.length>=2&&max>min&&step>=min)
                {
                    globalData?.device?.add(DeviceType(
                        deviceName = deviceName,
                        deviceMetaData = MetaData(
                            isRGB = isRGB,
                            rgbColor = rgbColor,
                            range = Range(
                                min=min.toInt(),
                                max=max.toInt(),
                                value=value.toInt(),
                                stepSize = step.toInt()

                            )
                        )
                    ))
                    navController.popBackStack()
                    Toast.makeText(context,"Added",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context,"Please enter device name and max > min",Toast.LENGTH_SHORT).show()
                }
            })
            {
                Icon( Icons.Filled.Done, contentDescription = "")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
       OutlinedTextField(
           value = deviceName,
           singleLine = true,
           onValueChange = { deviceName = it.uppercase() },
           label = { Text("Device Name") },
           modifier = Modifier.fillMaxWidth()
       )
        Spacer(modifier = Modifier.height(16.dp))

        // RGB Switch
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable RGB", modifier = Modifier.weight(1f))
            Switch(
                checked = isRGB,
                onCheckedChange = {
                    isRGB = it
                    if (!isRGB) {
                        rgbColor = "#f00" // Reset to default if RGB is disabled
                        sliderValue = 0f
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Step: ${step.toInt()}", modifier = Modifier.padding(vertical = 8.dp))
        Slider(
            value = step,
            onValueChange = { value ->
                step = value
            },
            valueRange = 1f..max,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Min: ${min.toInt()}", modifier = Modifier.padding(vertical = 8.dp))
        Slider(
            value = min,
            onValueChange = { value ->
                min = value
            },
            valueRange = 1f..max,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Max: ${max.toInt()}", modifier = Modifier.padding(vertical = 8.dp))
        Slider(
            value = max,
            onValueChange = { value ->
                max = value
              },
            valueRange = 1f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Val: ${value.toInt()}", modifier = Modifier.padding(vertical = 8.dp))
        Slider(
            value = value,
            onValueChange = { valu->
                value= valu
            },
            valueRange = min..max,
            steps = max.toInt(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // RGB Slider
        if (isRGB) {
            Text("RGB Color Value: $rgbColor", modifier = Modifier.padding(vertical = 8.dp))
            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    sliderValue = value
                    rgbColor = String.format("#%02X%02X%02X", value.toInt(), 0, 0) // Example for red
                },
                valueRange = 0f..255f,
                steps = 255,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}




