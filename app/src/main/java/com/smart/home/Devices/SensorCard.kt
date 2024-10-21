package com.smart.home.Devices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smart.home.SensorMetaData
import com.smart.home.SnackbarManager



// Sensor Card Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorCard(
    sensor: SensorMetaData,
    index: Int,
    onSensorChange: (SensorMetaData) -> Unit,
    onRemoveSensor: () -> Unit
) {
    var priority by remember { mutableStateOf(sensor.priority) }
    var name by remember { mutableStateOf(sensor.name) }
    var digitaloranalog by remember { mutableStateOf(sensor.type) }
    var enabled by remember { mutableStateOf(sensor.enabled) }
    var expanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(false) }
    var triggerAt by remember { mutableStateOf(sensor.triggerAt.toString()) }
    var relayon by remember { mutableStateOf(sensor.relayon) }
    var sensorName by remember { mutableStateOf(sensor.sensorName) }
    var sensorNameExpand by remember { mutableStateOf(false) }
    val priorities = listOf(1, 2, 3, 4, 5)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween){
                Text("Sensor ${index + 1}", )
                Box(
                    modifier = Modifier.clickable  {
                        onRemoveSensor()
                    }.size(25.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White), contentAlignment = Alignment.Center // Background color
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Filled.Clear,
                        tint = Color.Black,
                        contentDescription = "Dropdown Icon"
                    )
                }


            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    name=newValue
                },
                singleLine = true,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = sensorNameExpand,
                onExpandedChange = { sensorNameExpand = !sensorNameExpand }
            ) {
                // The text field displaying the selected priority and drop-down arrow
                OutlinedTextField(
                    value = "Sensor: $sensorName",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { sensorNameExpand = !sensorNameExpand }) {
                            Icon(
                                imageVector = if (sensorNameExpand) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    },
                    modifier = Modifier
                        .menuAnchor() // Anchor for the dropdown
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = sensorNameExpand,
                    onDismissRequest = { sensorNameExpand = false }
                ) {
                    listOf("PIR","LDR","TEMP","US").forEach {
                        DropdownMenuItem(onClick = { }
                            ,text={
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.clickable {
                                        sensorName=it
                                        sensorNameExpand=false
                                    }
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp))
                                {
                                    Text(text = "Sensor Name $it")
                                }
                            })


                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                // The text field displaying the selected priority and drop-down arrow
                OutlinedTextField(
                    value = "Priority: $priority",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    },
                    modifier = Modifier
                        .menuAnchor() // Anchor for the dropdown
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    priorities.forEach {
                        DropdownMenuItem(onClick = {

                        }
                            ,text={
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.clickable {
                                        priority=it
                                        expanded=false
                                    }
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp))
                                {
                                    Text(text = "Priority $it")
                                }
                            })


                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enabled Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled: ")
                Switch(
                    checked = enabled == 1,
                    onCheckedChange = { newEnabled ->
                        enabled = if (newEnabled) 1 else 0
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Type Selector
            ExposedDropdownMenuBox(
                expanded = type,
                onExpandedChange = { type = !type }
            ) {

                OutlinedTextField(
                    value = "Type: ${digitaloranalog}",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { type = !type }) {
                            Icon(
                                imageVector = if (type) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    },
                    modifier = Modifier
                        .menuAnchor() // Anchor for the dropdown
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = type,
                    onDismissRequest = { type = false }
                ) {
                    listOf("Analog","Digital").forEach {
                        DropdownMenuItem(onClick = {

                        }
                            ,text={
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.clickable {
                                        digitaloranalog=it
                                        type=false
                                    }
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp))
                                {
                                    Text(text = "Priority $it")
                                }
                            })


                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger At Input
            OutlinedTextField(
                value = triggerAt,
                onValueChange = { newValue ->
                    triggerAt=newValue
                },
                singleLine = true,
                label = { Text("Trigger At") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Relay On Input
            OutlinedTextField(
                value = relayon,
                onValueChange = { newValue ->
                    relayon=newValue
                 },
                singleLine = true,
                label = { Text("Relay On") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                     if(priority>0&&digitaloranalog.isNotEmpty()&&triggerAt.isNotEmpty()&&name.isNotEmpty())
                     {
                         onSensorChange(
                             SensorMetaData(
                                 name=name,
                                 triggerAt = triggerAt.toInt(),
                                 type=digitaloranalog,
                                 enabled=enabled,
                                 relayon = relayon,
                                 priority = priority,
                                 sensorName = sensorName
                             )
                         )
                     }
                    else{
                        SnackbarManager.showSnackbar("Please fill all details")
                     }
                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text(text = "Ok")
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}



