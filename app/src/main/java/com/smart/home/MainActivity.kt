package com.smart.home

import WebSocketService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier 
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.smart.home.ui.theme.HomeTheme
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private lateinit var webSocketClient: WebSocketService
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedViewModel: SharedViewModel by viewModels()

        // Initialize WebSocketService with the SharedViewModel
        webSocketClient = WebSocketService(this)
        webSocketClient.connectWebSocket(sharedViewModel)

//        enableEdgeToEdge()
        setContent {
            HomeTheme {
             NavHost(webSocketClient,sharedViewModel)
            }
        }
    }

    override fun onDestroy() {
        webSocketClient.closeWebSocket()
        super.onDestroy()
    }
}



@Composable
fun GaugeSlider(
    modifier: Modifier = Modifier,
    value: Float, // Value from 0 to 100
    onValueChange: (Float) -> Unit,
    strokeWidth: Float = 20f, // Gauge stroke width
    gaugeColor: Color = Color.Gray,
    progressColor: Color = Color.Blue
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
        ) {
            // Draw the full gauge (circle)
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Draw the progress on the gauge
            drawArc(
                color = progressColor,
                startAngle = 135f,
                sweepAngle = (270 * (value / 100)), // Progress is proportional to the value
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Draw the thumb (knob) on the arc
            val angleInRadians = ((135f + 270f * (value / 100)) * PI / 180).toFloat()
            val radius = size.width / 2f - strokeWidth / 2
            val knobX = radius * cos(angleInRadians) + size.width / 2
            val knobY = radius * sin(angleInRadians) + size.height / 2
            drawCircle(
                color = Color.Red,
                radius = strokeWidth / 2,
                center = Offset(knobX - 10, knobY - 10)
            )
        }
    }

    // Slider underneath the gauge for controlling the value
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..100f,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.8f)
    )
}

@Composable
fun GaugeSliderScreen() {
    var sliderValue by remember { mutableStateOf(50f) } // Initial value

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GaugeSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            strokeWidth = 25f,
            gaugeColor = Color.LightGray,
            progressColor = Color.Green
        )

        Text(text = "Value: ${sliderValue.toInt()}")
    }
}

@Composable
fun Screen1(navController: NavHostController, sharedViewModel: SharedViewModel) {
    val message by sharedViewModel.message.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Screen 1")
        Text(text = "Message: $message")
        Button(onClick = {
            // Navigate to Screen 2
            navController.navigate("screen2")
        }) {
            Text("Go to Screen 2")
        }
        Button(onClick = {
            // Update the message in the ViewModel
            sharedViewModel.updateMessage("Updated Message from Screen 1")
        }) {
            Text("Update Message")
        }
        GaugeSliderScreen()
    }
}

@Composable
fun Screen2(navController: NavHostController, sharedViewModel: SharedViewModel) {
    val message by sharedViewModel.message.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Screen 2")
        Text(text = "Message: $message")
        Button(onClick = {
            // Update the message in the ViewModel
            sharedViewModel.updateMessage("Message updated from Screen 2")
        }) {
            Text("Update Message")
        }
        Button(onClick = {
            // Navigate back to Screen 1
            navController.popBackStack()
        }) {
            Text("Go back to Screen 1")
        }
    }
}

@Composable
fun Screen3(navController: NavHostController, sharedViewModel: SharedViewModel) {
    val message by sharedViewModel.message.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Screen 2")
        Text(text = "Message: $message")
        Button(onClick = {
            // Update the message in the ViewModel
            sharedViewModel.updateMessage("Message updated from Screen 2")
        }) {
            Text("Update Message")
        }
        Button(onClick = {
            // Navigate back to Screen 1
            navController.popBackStack()
        }) {
            Text("Go back to Screen 1")
        }
    }

}


