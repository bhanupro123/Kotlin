package com.smart.home.Devices

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TVControlUI() {
    var isFanOn by remember { mutableStateOf(false) }
    var fanSpeed by remember { mutableStateOf(0f) }
    var timeoutSeconds by remember { mutableStateOf(0) }
    var fanRotation by remember { mutableStateOf(0f) }
    var timeoutRunning by remember { mutableStateOf(false) }

    // Animate fan rotation based on speed
    val animatedRotation = animateFloatAsState(
        targetValue = if (isFanOn) fanSpeed * 360 else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    // Timeout countdown
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fan icon with rotation animation
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.LightGray, shape = CircleShape)
                .rotate(animatedRotation.value),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Air, // Fan Icon
                contentDescription = "Fan Icon",
                modifier = Modifier.size(100.dp),
                tint = if (isFanOn) Color.Blue else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // On/Off Button
        Button(
            onClick = {
                isFanOn = !isFanOn
                if (!isFanOn) {
                    fanSpeed = 0f
                    timeoutRunning = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                contentColor = if (isFanOn) Color.Red else Color.Green
            )
        ) {
            Text(if (isFanOn) "Turn Off" else "Turn On")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fan Speed Control
        AnimatedVisibility(visible = isFanOn) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Speed: ${fanSpeed.toInt()}")
                Slider(
                    value = fanSpeed,
                    onValueChange = { fanSpeed = it },
                    valueRange = 0f..5f,
                    steps = 4,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timeout Control
        AnimatedVisibility(visible = isFanOn) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Timeout: ${timeoutSeconds}s")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { timeoutSeconds += 10 }) {
                        Text("+10s")
                    }
                    Button(onClick = {
                        if (timeoutSeconds > 0) {
                            timeoutRunning = true
                        }
                    }) {
                        Text("Start Timeout")
                    }
                    Button(onClick = {
                        timeoutRunning = false
                        timeoutSeconds = 0
                    }) {
                        Text("Cancel Timeout")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovableCircularGauge(fanSpeed: Float, onFanSpeedChange: (Float) -> Unit) {
    val size = 150
    val strokeWidth = 10.dp
    val maxSpeed = 5f // Maximum speed for gauge
    var fanSpeed1 by remember { mutableStateOf(fanSpeed) } // Fan speed value
    val buttonRadius = size/2
    val angle=(fanSpeed1 / maxSpeed) * 360
    var pointerPosition by remember { mutableStateOf( angle)}
    val startAngle=135f
    Canvas(modifier = Modifier
        .size(size.dp)
    ) {
        // Draw the circular arc representing speed
        val radius = size.dp.toPx() / 2 - strokeWidth.toPx() / 2

        // Draw the background circle
        drawCircle(
            color = Color.Gray,
            radius = radius + strokeWidth.toPx() / 2
        )
        drawArc(
            color = Color.LightGray,
            startAngle = startAngle, // Start from left-side top
            sweepAngle = 270f, // Sweep based on the fan speed
            useCenter = false,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        // Draw the gauge arc (circular segment)
        drawArc(
            color = Color.Black,
            startAngle = startAngle, // Start from left-side top
            sweepAngle = angle, // Sweep based on the fan speed
            useCenter = false,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                (fanSpeed1+1).toInt().toString(),
                size.dp.toPx() / 2,
                size.dp.toPx() / 2 + 10, // Offset for vertical alignment
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
    for (i in 0..<maxSpeed.toInt()){
        val angle1 = (((360/5))*(i+maxSpeed.toInt()) * Math.PI / 180) // Convert to radians
        val xOffset = cos(angle1+45) * buttonRadius
        val yOffset = sin(angle1+45) * buttonRadius
        Box(
            modifier = Modifier
                .offset(x = (xOffset).dp, y = (yOffset).dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(if(fanSpeed1.toInt()+1==i||( i==0&&fanSpeed1.toInt()+1==5))Color.Blue else Color.LightGray)
                .clickable {
                    val newSpeed = if(i==0) 5f else i.toFloat()
                    fanSpeed1=newSpeed-1
                    onFanSpeedChange(newSpeed)
                    pointerPosition  =1f
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${if(i==0) 5 else i}",
                color =if(fanSpeed1.toInt()+1==i||( i==0&&fanSpeed1.toInt()+1==5)) Color.White else Color.Black,
                fontSize = 14.sp)
        }
    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovableCircularGauge2(fanSpeed: Float, onFanSpeedChange: (Float) -> Unit) {
    val size = 150.dp
    val strokeWidth = 20.dp
    val maxSpeed = 5f // Maximum speed for gauge

    // Calculate angle based on the fan speed
    val angle = (fanSpeed / maxSpeed) * 360

    // Movable pointer position
    var pointerPosition by remember { mutableStateOf(angle) }
    var isCrossed by remember { mutableStateOf("") }

    Canvas(modifier = Modifier
        .size(size)
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                // Get the center point of the gauge
                val centerX = size.toPx() / 2
                val centerY = size.toPx() / 2

                // Calculate the new angle based on the pointer's position
                val newAngle = atan2(
                    (change.position.y - centerY),
                    (change.position.x - centerX)
                ) * (180 / Math.PI) + 90 // Shift by 90 degrees for correct orientation

                // Store the previous position
                val previousPointerPosition = newAngle.toFloat().coerceIn(0f, 360f)

                if (previousPointerPosition in 1f..270f) {
                    pointerPosition = newAngle.toFloat().coerceIn(0f, 360f)
                    onFanSpeedChange((pointerPosition / 360) * maxSpeed)
                }
            }
        }
    ) {
        // Draw the circular arc representing speed
        val radius = size.toPx() / 2 - strokeWidth.toPx() / 2
        val startAngle=135f
        // Draw the gauge arc (circular segment)
        drawArc(
            color = Color.Gray,
            startAngle = startAngle, // Start from left-side top
            sweepAngle = angle, // Sweep based on the fan speed
            useCenter = false,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Draw the pointer line (fan blade indicator)
        val pointerRadius = radius - strokeWidth.toPx() / 2 // Adjust for stroke
        val pointerX = pointerRadius * cos(Math.toRadians(pointerPosition.toDouble() +startAngle)) + size.toPx() / 2
        val pointerY = pointerRadius * sin(Math.toRadians(pointerPosition.toDouble() +startAngle)) + size.toPx() / 2

        drawLine(
            color = Color.Red,
            start = Offset(size.toPx() / 2, size.toPx() / 2),
            end = Offset(pointerX.toFloat(), pointerY.toFloat()),
            strokeWidth = 8f
        )

        // Draw the rounded pointer thumb (circular knob)
        val thumbRadius = 15f // Size of the thumb
        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(pointerX.toFloat() - thumbRadius, pointerY.toFloat() - thumbRadius),
            size = Size(thumbRadius * 2, thumbRadius * 2),
            cornerRadius = CornerRadius(thumbRadius, thumbRadius)
        )

        // Draw the current speed text in the center
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", fanSpeed),
                size.toPx() / 2,
                size.toPx() / 2 + 10, // Offset for vertical alignment
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovableCircularGauge3(fanSpeed: Float, onFanSpeedChange: (Float) -> Unit) {
    val size = 150
    val strokeWidth = 20.dp
    val maxSpeed = 5f // Maximum speed for gauge
    var fanSpeed1 by remember { mutableStateOf(fanSpeed) } // Fan speed value
    val buttonRadius = size/2
    val angle=(fanSpeed1 / maxSpeed) * 360
    var pointerPosition by remember { mutableStateOf( angle)}
    val startAngle=135f
    Canvas(modifier = Modifier
        .size(size.dp)
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                // Get the center point of the gauge
                val centerX = size.dp.toPx() / 2
                val centerY = size.dp.toPx() / 2

                // Calculate the new angle based on the pointer's position
                val newAngle = atan2(
                    (change.position.y - centerY),
                    (change.position.x - centerX)
                ) * (180 / Math.PI) + 90 // Shift by 90 degrees for correct orientation

                // Store the previous position
                val previousPointerPosition = newAngle.toFloat().coerceIn(0f, 360f)

                if (previousPointerPosition in 1f..270f) {
                    pointerPosition = newAngle.toFloat().coerceIn(0f, 360f)
                    onFanSpeedChange((pointerPosition / 360) * maxSpeed)
                }
            }
        }
    ) {
        // Draw the circular arc representing speed
        val radius = size.dp.toPx() / 2 - strokeWidth.toPx() / 2

        // Draw the background circle
        drawCircle(
            color = Color.LightGray,
            radius = radius + strokeWidth.toPx() / 2
        )
        drawArc(
            color = Color(0xFFf3f3f3),
            startAngle = startAngle, // Start from left-side top
            sweepAngle = 270f, // Sweep based on the fan speed
            useCenter = false,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        // Draw the gauge arc (circular segment)
        drawArc(
            color = Color.Gray,
            startAngle = startAngle, // Start from left-side top
            sweepAngle = angle, // Sweep based on the fan speed
            useCenter = false,
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        println(pointerPosition)
        // Draw the pointer line (fan blade indicator)
        //  val pointerRadius = radius - strokeWidth.toPx() / 2 // Adjust for stroke
        //   val pointerX = pointerRadius * cos(Math.toRadians(pointerPosition.toDouble() +startAngle)) + size.dp.toPx() / 2
//        val pointerY = pointerRadius * sin(Math.toRadians(pointerPosition.toDouble() +startAngle)) + size.dp.toPx() / 2
//
//        drawLine(
//            color = Color.Red,
//            start = Offset(size.dp.toPx() / 2, size.dp.toPx() / 2),
//            end = Offset(pointerX.toFloat(), pointerY.toFloat()),
//            strokeWidth = 8f
//        )
//
//        // Draw the rounded pointer thumb (circular knob)
//     val thumbRadius = 15f // Size of the thumb
//        drawRoundRect(
//            color = Color.Red,
//            topLeft = Offset(pointerX.toFloat() - thumbRadius, pointerY.toFloat() - thumbRadius),
//            size = Size(thumbRadius * 2, thumbRadius * 2),
//            cornerRadius = CornerRadius(thumbRadius, thumbRadius)
//        )

        // Draw the current speed text in the center
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                (fanSpeed1+1).toInt().toString(),
                size.dp.toPx() / 2,
                size.dp.toPx() / 2 + 10, // Offset for vertical alignment
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
            )
        }


    }


    val index=((pointerPosition / 360) * maxSpeed).toInt()
    for (i in 0..<maxSpeed.toInt()){
        val angle1 = (((360/5))*(i+maxSpeed.toInt()) * Math.PI / 180) // Convert to radians

        val xOffset = cos(angle1+45) * buttonRadius
        val yOffset = sin(angle1+45) * buttonRadius

        Box(
            modifier = Modifier
                .offset(x = (xOffset).dp, y = (yOffset).dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(if(fanSpeed1.toInt()+1==i)Color.Blue else Color.LightGray)
                .clickable {

                    val newSpeed = if(i==0) 5f else i.toFloat()
                    fanSpeed1=newSpeed-1
                    onFanSpeedChange(newSpeed)
                    pointerPosition  =1f

                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${if(i==0) 5 else i}", color =if(fanSpeed1.toInt()+1==i)Color.White else Color.Black,
                fontSize = 14.sp)
        }
    }
}
