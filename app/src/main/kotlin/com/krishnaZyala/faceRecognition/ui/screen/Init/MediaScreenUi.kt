package com.krishnaZyala.faceRecognition.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.krishnaZyala.faceRecognition.ui.screen.Init.DualStreamViewModel
import com.krishnaZyala.faceRecognition.ui.screen.Init.DualStreamViewModelFactory

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VideoReceiverScreen() {
    val context = LocalContext.current
    val viewModel: DualStreamViewModel = viewModel(
        factory = DualStreamViewModelFactory(context.applicationContext)
    )
    val bitmap by viewModel.bitmap.collectAsState()

    val isMuted by viewModel.isAudioMuted.collectAsState()
    val isVideoEnabled by viewModel.isVideoEnabled.collectAsState()
    LaunchedEffect(Unit) {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startCamera(context)
            viewModel.startSendingAudio(context)
        }
        else{
            Toast.makeText(context,"failed",Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                Column {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Live Video",
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { viewModel.toggleMute() }) {
                        Text(if (isMuted) "Unmute" else "Mute")
                    }

                    Button(onClick = { viewModel.toggleVideo() }) {
                        Text(if (isVideoEnabled) "Turn Video Off" else "Turn Video On")
                    }
                }

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Waiting for other device's video stream...",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )


                }
            }
        }
    }
}
