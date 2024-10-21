package com.smart.home.Devices


import WebSocketService
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.notifii.lockers.Utils.getRandomColor
import com.smart.home.Category
import com.smart.home.DeviceMetaInfo
import com.smart.home.DeviceTypeModel
import com.smart.home.PairDevice
import com.smart.home.ScheduleMetaData
import com.smart.home.SensorMetaData
import com.smart.home.SnackbarManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.lang.reflect.Array.set
import java.time.LocalTime
import java.time.format.DateTimeFormatter



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeviceConfiguration( webSocketClient: WebSocketService,
                         deviceMetaInfo:DeviceMetaInfo,
                         onDeviceChange: (DeviceMetaInfo) -> Unit,
                         onDone: (ScheduleMetaData) -> Unit,
                         isItNew:Boolean=false ,category: Category =Category()
)
{
    val tableName="schedule_"+category.id.replace(" ","_")+"_"+deviceMetaInfo.name.replace(" ","_")
    var enabled by remember { mutableStateOf(deviceMetaInfo.enabled) }
    var range by remember { mutableStateOf(deviceMetaInfo.range) }
    var destination by remember { mutableStateOf(deviceMetaInfo.destination) }
    var schedule by remember { mutableStateOf(deviceMetaInfo.schedule) }
    var addSchedule by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(-1) }
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp))
    {
        Column(
            modifier =  Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(10.dp)).background(Color.DarkGray)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Pair Device Configuration")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Enabled: ")
                Switch(
                    checked = enabled==1,
                    onCheckedChange = {
                        enabled = if(it) 1 else 0
                        onDeviceChange(deviceMetaInfo.copy(enabled = if(it) 1 else 0))
                    }
                )
            }
            Text(text = "Destination: ") // Display current value
            OutlinedTextField(
                value = deviceMetaInfo.destination,
                singleLine = true,
                onValueChange = {
                    destination=it.uppercase()
                    onDeviceChange(deviceMetaInfo.copy(destination=it.uppercase()))
                },
                label = { Text("Device Destination") },
                modifier = Modifier.fillMaxWidth()
            )
            if(deviceMetaInfo.type=="FAN"&&isItNew)
            {
                Spacer(modifier = Modifier.height(10.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth())
                {
                    MovableCircularGauge(
                        fanSpeed = deviceMetaInfo.range.toFloat(),
                        onFanSpeedChange = {
                            deviceMetaInfo.range = it.toInt()
                            onDeviceChange(deviceMetaInfo.copy(range=it.toInt()))
                        }
                    )
                }

            }
            else if( deviceMetaInfo.type.isEmpty())
            {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Range: ${range}") // Display current value
                Slider(
                    value = range.toFloat(),
                    onValueChange = { range = it.toInt() },
                    valueRange = 1f..  100f, // Adjust the range as needed
                    steps =   99 // 100 steps
                )
            }
            deviceMetaInfo.schedule.forEachIndexed { index, it ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation =CardDefaults.cardElevation(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(16.dp)) {

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
                        {
                            Text(text = "Schedule Id: ${it.id}",  style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = {
                                selectedIndex=index
                            })
                            {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                val jsonObject = JsonObject(mapOf(
                                    "id" to JsonPrimitive(it.id),
                                    "tableName" to JsonPrimitive(tableName)
                                ))
                                val objectString = Json.encodeToString(JsonObject.serializer(), jsonObject)
                                webSocketClient.sendMessage("deleteschedule_$objectString")
                            })
                            {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Edit Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(text = "Name: ${it.name}",   style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically)
                        {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Time Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(text = "StartTime: ${it.startTime}",  style = MaterialTheme.typography.titleMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically)
                        {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Time Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(text = "EndTime: ${it.endTime}",  style = MaterialTheme.typography.titleMedium)
                        }
                        Text(text = "Priority: ${it.priority}",  style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            if(!isItNew&&addSchedule.isEmpty())
            {
                Button(
                    onClick = {
                        addSchedule="add"
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(text = "Add Schedule")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

        }

        AnimatedVisibility(visible = addSchedule=="add"||addSchedule=="edit",
            modifier = Modifier.weight(1f).fillMaxHeight())
        {

            Column(
                modifier =  Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(10.dp)).background(Color.DarkGray)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScheduleCard(
                    tableName=tableName,
                    edit=(addSchedule=="edit"),
                    webSocketClient = webSocketClient,
                    deviceMetaInfo=deviceMetaInfo,
                    schedule = if(addSchedule=="edit")  schedule[selectedIndex] else ScheduleMetaData(),
                    onDone = {
                        addSchedule=""
                    }
                )

            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleCard(tableName:String="",
                 webSocketClient: WebSocketService,
                 edit:Boolean=false,
                 deviceMetaInfo:DeviceMetaInfo,
                 schedule: ScheduleMetaData,
                 onDone: () -> Unit) {
    var enabled by remember { mutableStateOf(schedule.enabled) }
    var startTime by remember { mutableStateOf(LocalTime.parse(schedule.startTime)) }
    var endTime by remember { mutableStateOf(LocalTime.parse(schedule.endTime)) }
    var priority by remember { mutableStateOf(schedule.priority) }
    var name by remember { mutableStateOf(schedule.name) }
    var sensors by remember { mutableStateOf(schedule.sensors) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val priorities = listOf(1, 2, 3, 4, 5)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Schedule #${schedule.id}")

            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    name=newValue
                },
                singleLine = true,
                label = { Text("Schedule Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )



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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled: ")
                Switch(
                    checked = enabled==1,
                    onCheckedChange = { newEnabled ->
                        enabled = if(newEnabled) 1 else 0
                    }
                )
            }

            Text(
                text = "Start Time: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                modifier = Modifier
                    .clickable { showStartTimePicker = true }
                    .padding(vertical = 8.dp)
            )

            // End Time - Clickable Text with TimePicker
            Text(
                text = "End Time: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                modifier = Modifier.clickable { showEndTimePicker = true }
                    .padding(vertical = 8.dp)
            )

            // Switch for deviceMode

            // isRGB - Switch


            if (showStartTimePicker) {
                TimePickerDialog(
                    time = startTime,
                    onTimeSelected = {
                        startTime = it
                        showStartTimePicker = false
                    },
                    onDismiss = { showStartTimePicker = false }
                )
            }

            if (showEndTimePicker) {
                TimePickerDialog(
                    time = endTime,
                    onTimeSelected = {
                        endTime = it
                        showEndTimePicker = false
                    },
                    onDismiss = { showEndTimePicker = false }
                )
            }
            sensors.forEachIndexed { index, sensor ->
                SensorCard(
                    sensor = sensor,
                    index = index,
                    onSensorChange = { updatedSensor ->
                        sensors=(sensors.toMutableList().apply {
                            set(index, updatedSensor)
                        })

                    },
                    onRemoveSensor = {
                        sensors=sensors.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
            }

            Button(
                onClick = {
                    if(
                        sensors.all { sensor ->
                            sensor.name.isNotBlank() &&
                                    sensor.priority in 1..5 &&
                                    sensor.triggerAt >=0 &&
                                    sensor.type.isNotBlank()&&sensor.sensorName.isNotEmpty()
                        })
                        sensors=(sensors + SensorMetaData())
                    else
                        SnackbarManager.showSnackbar("Fill all fields")

                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text(text = "Add more sensors")
            }

            Button(
                onClick = {
                    if(edit)
                        webSocketClient.sendMessage("editschedule_"+GsonBuilder().create().toJson(ScheduleMetaData(
                            sensors = sensors,
                            name = name,
                            tableName=tableName,
                            enabled = enabled,
                            startTime= startTime.toString(),
                            endTime = endTime.toString(),
                            priority = priority,
                            deviceMetaInfo = DeviceMetaInfo()
                        )))
                    else {
                        webSocketClient.sendMessage("insertschedule_"+GsonBuilder().create().toJson(
                            listOf( ScheduleMetaData(
                                tableName=tableName,
                                sensors = sensors,
                                name = name,
                                enabled = enabled,
                                startTime= startTime.toString(),
                                endTime = endTime.toString(),
                                priority = priority,
                                deviceMetaInfo = DeviceMetaInfo()
                            ))
                        )
                        )

                    }
                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text(text = if(edit) "Edit" else "Save")
            }
            Button(
                onClick = {
                    val schedule = ScheduleMetaData(
                        deviceMetaInfo = DeviceMetaInfo(),
                        enabled = 1,
                        endTime = "06:00",
                        tableName = tableName,
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
                    webSocketClient.sendMessage( "insertschedule_"+GsonBuilder().create().toJson(listOf(schedule)))

                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text(text = "Save 1")
            }



        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimePickerDialog(
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(LocalTime.of(hour, minute))
        },
        time.hour,
        time.minute,
        true
    )

    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose {
            timePickerDialog.dismiss()
        }
    }
}
