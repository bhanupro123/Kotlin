package com.smart.home.Devices

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun FanControlUIMulti(fanOn:Boolean=true) {
    var isFanOn by remember { mutableStateOf(fanOn) }

    var fanSpeed by remember { mutableStateOf(1f) } // Fan speed value
    var timeoutSeconds by remember { mutableStateOf(0) }
    var timeoutRunning by remember { mutableStateOf(false) }
    val rotationDegrees = remember { Animatable(0f) }


    LaunchedEffect(fanSpeed,isFanOn) {
        if (isFanOn) {
            while (true) {
                rotationDegrees.animateTo(
                    targetValue = rotationDegrees.value + 360f,
                    animationSpec = tween(durationMillis = (1000 / fanSpeed).toInt(), easing = LinearEasing)
                )
            }
        } else {
            // Reset rotation when fan is off
            rotationDegrees.snapTo(0f)
        }
    }
    LaunchedEffect(timeoutRunning) {
        if (timeoutRunning) {
            while (timeoutSeconds > 0) {
                delay(1000L)
                timeoutSeconds -= 1
            }
            isFanOn = false
            timeoutRunning = false
        }
    }


    Row( modifier = Modifier
        .fillMaxSize())
    {
        Column(
            modifier = Modifier
                .fillMaxHeight().weight(1f)
                .clip(RoundedCornerShape(10.dp)).background(Color.DarkGray)
                .padding( horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =if(isFanOn) Arrangement.SpaceBetween else Arrangement.spacedBy(10.dp,Alignment.CenterVertically)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, shape = CircleShape)
                    .rotate(rotationDegrees.value), // Rotate based on current animation value
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Emergency, // Fan Icon
                    contentDescription = "Fan Icon",
                    modifier = Modifier.size(50.dp),
                    tint = if (isFanOn) Color.Blue else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedVisibility(visible = isFanOn) {

                Box(contentAlignment = Alignment.Center)
                {
                    MovableCircularGauge(
                        fanSpeed = fanSpeed,
                        onFanSpeedChange = { newSpeed -> fanSpeed = newSpeed }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    isFanOn = !isFanOn
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFanOn) Color.Red else Color.Green
                )
            ) {
                Text(if (isFanOn) "Turn Off" else "Turn On")
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}



