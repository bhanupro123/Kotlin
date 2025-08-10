package com.krishnaZyala.faceRecognition.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.rtp.AudioStream
import android.net.wifi.WifiManager
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.krishnaZyala.faceRecognition.data.model.AppState
import com.krishnaZyala.faceRecognition.ui.navigation.Routes
import com.krishnaZyala.faceRecognition.ui.screen.Init.VoiceWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.handshake.ServerHandshake
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.URI

@SuppressLint("ServiceCast")
fun getLocalIpAddress(context: Context): String? {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ipInt = wifiManager.connectionInfo.ipAddress
    // Convert little-endian int IP to string like "192.168.1.5"
    return if (ipInt != 0) Formatter.formatIpAddress(ipInt) else null
}
@Composable
fun AudioStream(appState: AppState) {
    val context = LocalContext.current
    val statusText by remember { mutableStateOf("Tap to speak...") }
    val serverIp = "192.168.0.149"
    val uri = URI("ws://$serverIp:12345")

    var connectionStatus by remember { mutableStateOf("Connecting...") }
    var isMuted by remember { mutableStateOf(true) }

    // ✅ Declare client as a regular variable (not remember)
    var client: VoiceWebSocket? = null

    // ✅ Connect only when screen is shown
    LaunchedEffect(Unit) {
        client = object : VoiceWebSocket(context, uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                super.onOpen(handshakedata)
                connectionStatus = "✅ Connected"
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                super.onClose(code, reason, remote)
                connectionStatus = "❌ Disconnected: $reason"
            }

            override fun onError(ex: Exception?) {
                super.onError(ex)
                ex?.printStackTrace()
                connectionStatus = "⚠️ Error: ${ex?.message}"
            }
        }.apply {
            connect()
            setMute(isMuted)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡 WebSocket Voice Client", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text("Status: $connectionStatus", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Text(text = getLocalIpAddress(context).toString(), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Text(text = statusText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "ws://$serverIp:12345", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            isMuted = !isMuted
            client?.setMute(isMuted)
        }) {
            Text(if (isMuted) "🔇 Microphone muted" else "🎤 Microphone unmuted")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {   }) {
            Text("A")
        }

        Button(onClick = {   }) {
            Text("B")
        }
    }
}