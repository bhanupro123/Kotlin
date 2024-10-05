package com.smart.home.Devices


import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.smart.home.GlobalSharedViewModel
import com.smart.home.GlobalViewModel
import com.smart.home.PairDevice
import com.smart.home.ScheduleMetaData
import java.time.LocalTime
import java.time.format.DateTimeFormatter



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeviceConfiguration( deviceConfig:PairDevice,
                         onDeviceChange: (PairDevice) -> Unit,
                          ) {
    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(10.dp)).background(Color.DarkGray)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Pair Device Configuration")
        PairDeviceUI(
            pairDevice = deviceConfig,
            onDeviceChange = onDeviceChange
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PairDeviceUI( pairDevice: PairDevice, onDeviceChange: (PairDevice) -> Unit) {

    Column(modifier = Modifier.padding(16.dp)) {


        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Enabled: ")
            Switch(
                checked = pairDevice.enabled,
                onCheckedChange = {
                    onDeviceChange(pairDevice.copy(enabled = it))
                }
            )
        }

        // Global Status Switch
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Global Status: ")
            Switch(
                enabled = false,
                checked = pairDevice.globalStatus,
                onCheckedChange = {
                    onDeviceChange(pairDevice.copy(globalStatus = it))
                }
            )
        }

        // Render Schedules
        pairDevice.schedule.forEachIndexed { index, schedule ->
            ScheduleCard(
                pairDevice=pairDevice,
                schedule = schedule,
                index = index,
                onScheduleChange = { updatedSchedule ->
                    val updatedSchedules = pairDevice.schedule.toMutableList().apply {
                        set(index, updatedSchedule)
                    }
                    onDeviceChange(pairDevice.copy(schedule = updatedSchedules))
                }
            )
        }

        // Button to add new Schedule
        Button(
            onClick = {
                val newSchedule = ScheduleMetaData()
                onDeviceChange(pairDevice.copy(schedule = pairDevice.schedule + newSchedule))
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Add Schedule")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleCard(pairDevice:PairDevice,schedule: ScheduleMetaData, index: Int, onScheduleChange: (ScheduleMetaData) -> Unit) {
    var enabled by remember { mutableStateOf(schedule.enabled) }
    var startTime by remember { mutableStateOf(LocalTime.parse(schedule.startTime)) }
    var endTime by remember { mutableStateOf(LocalTime.parse(schedule.endTime)) }
    var deviceMode by remember { mutableStateOf(schedule.deviceMode) }
    val range=pairDevice.deviceType.deviceMetaData.range
    var level by remember { mutableStateOf(range.value.toFloat()) }
    var isRGB by remember { mutableStateOf(schedule.metaData.isRGB) }
    var rgbColor by remember { mutableStateOf(schedule.metaData.rgbColor) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Schedule #${index + 1}")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Enabled: ")
                Switch(
                    checked =enabled, // Assuming `status` can be represented as a boolean
                    onCheckedChange = { it ->
                        enabled = it
                        onScheduleChange(schedule.copy(enabled = it))
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // Status - Text Input


            // Start Time - Clickable Text with TimePicker
            Text(
                text = "Start Time: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                modifier = Modifier
                    .clickable { showStartTimePicker = true }
                    .padding(vertical = 8.dp)
            )

            // End Time - Clickable Text with TimePicker
            Text(
                text = "End Time: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                modifier = Modifier
                    .clickable { showEndTimePicker = true }
                    .padding(vertical = 8.dp)
            )

            // Switch for deviceMode
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Device Mode: ")
                Switch(
                    checked = deviceMode,
                    onCheckedChange = {
                        deviceMode = it
                        onScheduleChange(schedule.copy(deviceMode = it))
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // isRGB - Switch
            if(pairDevice.deviceType.deviceMetaData.isRGB)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = "Is RGB: ")
                Switch(
                    checked = isRGB,
                    onCheckedChange = {
                        isRGB = it
                        onScheduleChange(schedule.copy(metaData = schedule.metaData.copy(isRGB = it)))
                    }
                )
            }
            Text(text = "Range: ${level.toInt()} Min: ${range.min} Max: ${range.max}")
            Slider(
                value = level,
                onValueChange = { it->
                    level=it
                    onScheduleChange(schedule.copy(metaData = schedule.metaData.copy(range
                            =schedule.metaData.range.copy(value =it.toInt()))))
                },
                valueRange =range.min.toFloat()..range.max.toFloat(), // Adjust the range based on your needs
                steps =range.max-1 , // Optional: to create discrete steps
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // RGB Color - TextField
            if (isRGB) {
                TextField(
                    value = rgbColor,
                    onValueChange = {
                        rgbColor = it
                        onScheduleChange(schedule.copy(metaData = schedule.metaData.copy(rgbColor = it)))
                    },
                    label = { Text("RGB Color") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }

            // Show TimePickerDialogs if triggered
            if (showStartTimePicker) {
                TimePickerDialog(
                    time = startTime,
                    onTimeSelected = {
                        startTime = it
                        onScheduleChange(schedule.copy(startTime = it.toString()))
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
                        onScheduleChange(schedule.copy(endTime = it.toString()))
                        showEndTimePicker = false
                    },
                    onDismiss = { showEndTimePicker = false }
                )
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
