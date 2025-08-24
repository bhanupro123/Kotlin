package com.bhanupro.faceRecognition.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanupro.faceRecognition.ui.screen.Init.MultiStreamViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StreamRoom(viewModel: MultiStreamViewModel = viewModel()) {
    val context = LocalContext.current
    val streams by viewModel.streams.collectAsState()
    val selfBitmap by viewModel.selfBitmap.collectAsState()
    val focusedStreamId by viewModel.focusedStreamId.collectAsState()

    val isMuted by viewModel.isAudioMuted.collectAsState()
    val isVideoEnabled by viewModel.isVideoEnabled.collectAsState()

    val fullScreenStream = streams.find { it.senderId == focusedStreamId }

    LaunchedEffect(Unit) {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startCamera(context)
            viewModel.startSendingAudio(context)
        } else {
            Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAll()
        }
    }
    Scaffold {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (fullScreenStream != null) {
                // Show fullscreen dialog for focused stream
                AlertDialog(
                    onDismissRequest = { viewModel.clearFocus() },
                    confirmButton = {
                        Button(onClick = { viewModel.clearFocus() }) {
                            Text("Close")
                        }
                    },
                    title = {
                        Text(text = "🔍 ${fullScreenStream.senderId}")
                    },
                    text = {
                        Image(
                            bitmap = fullScreenStream.bitmap.asImageBitmap(),
                            contentDescription = "Fullscreen stream",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                )
            } else if (streams.isNotEmpty() || selfBitmap != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Show self stream
                    selfBitmap?.let { bmp ->
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📱 You (Bhanu)", color = Color.Blue)
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Your Camera",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        }
                    }

                    // Show other remote streams
                    items(streams, key = { it.senderId }) { stream ->
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clickable { viewModel.focusStream(stream.senderId) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📡 ${stream.senderId}", color = Color.Gray)
                            Image(
                                bitmap = stream.bitmap.asImageBitmap(),
                                contentDescription = "Stream from ${stream.senderId}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { viewModel.toggleMute() }) {
                                Text(if (isMuted) "Unmute" else "Mute")
                            }
                            Button(onClick = { viewModel.toggleVideo() }) {
                                Text(if (isVideoEnabled) "Video Off" else "Video On")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Waiting for video streams...",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
