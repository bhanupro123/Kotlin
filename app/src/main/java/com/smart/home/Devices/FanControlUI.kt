package com.smart.home.Devices

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun SmartHomeFanControl() {
    var isFanOn by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Schedule States
    var nightShiftEnabled by remember { mutableStateOf(false) }
    var customScheduleEnabled by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("Not Set") }
    var endTime by remember { mutableStateOf("Not Set") }
    var temperatureSensorEnabled by remember { mutableStateOf(false) }
    var pirSensorEnabled by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize().verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Smart Home Fan Control", fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Fan Control Section
        Button(
            onClick = { isFanOn = !isFanOn },
            colors = ButtonDefaults.buttonColors(containerColor = if (isFanOn) Color.Green else Color.Red),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (isFanOn) "Turn Off" else "Turn On", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Control Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = nightShiftEnabled, onCheckedChange = { nightShiftEnabled = it })
            Text(text = "Night Shift", fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = customScheduleEnabled, onCheckedChange = { customScheduleEnabled = it })
            Text(text = "Custom Schedule", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Pickers for Custom Schedule
        if (customScheduleEnabled) {
            Text(
                text = "Start Time: $startTime",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    showTimePicker(context) { selectedTime -> startTime = selectedTime }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "End Time: $endTime",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    showTimePicker(context) { selectedTime -> endTime = selectedTime }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sensor Controls
        Text(text = "Sensor Controls", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        SensorControl("Temperature Sensor", temperatureSensorEnabled) { isEnabled ->
            temperatureSensorEnabled = isEnabled
        }
        SensorControl("PIR Sensor", pirSensorEnabled) { isEnabled ->
            pirSensorEnabled = isEnabled
        }
        // Add individual sensor controls here
        // For example: PIR sensor, Temperature sensor, etc.
        SensorControl("Temperature Sensor")
        SensorControl("PIR Sensor")
    }
}
@Composable
fun SensorControl(sensorName: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isEnabled) Color.Green else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = sensorName, fontSize = 16.sp)
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Green,
                    uncheckedThumbColor = Color.Red
                )
            )
        }
    }
}

@Composable
fun SensorControl(sensorName: String) {
    var isEnabled by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
        Text(text = sensorName, fontSize = 16.sp)
    }
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
        },
        hour, minute, true
    ).show()
}
