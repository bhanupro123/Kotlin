package com.krishnaZyala.faceRecognition.ui.screen.dashBoard
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.app.admin.DevicePolicyManager
import com.krishnaZyala.faceRecognition.MyDeviceAdminReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlsModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)
    var isAdminActive by remember { mutableStateOf(devicePolicyManager.isAdminActive(componentName)) }

    // Volume state
    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // Brightness state - system brightness (0-255)
    var brightness by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                128
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device Controls", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))

            // Volume Control
            Text("Volume: $currentVolume / $maxVolume")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    enabled = currentVolume > 0,
                    onClick = {
                        if (currentVolume > 0) {
                            currentVolume -= 1
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                        }
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Volume Down")
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    enabled = currentVolume < maxVolume,
                    onClick = {
                        if (currentVolume < maxVolume) {
                            currentVolume += 1
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                        }
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Volume Up")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brightness Control
            Text("Brightness: $brightness / 255")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    enabled = brightness > 0,
                    onClick = {
                        if (brightness > 0) {
                            brightness = (brightness - 10).coerceAtLeast(0)
                            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                        }
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Brightness Down")
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    enabled = brightness < 255,
                    onClick = {
                        if (brightness < 255) {
                            brightness = (brightness + 10).coerceAtMost(255)
                            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                        }
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Brightness Up")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Device Admin Status: ${if (isAdminActive) "Active" else "Inactive"}")
            if (!isAdminActive) {
                Text(
                    "Activate Device Admin for full control",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Activate device admin for advanced controls")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }) {
                    Text("Activate Device Admin")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Close")
            }
        }
    }
}

