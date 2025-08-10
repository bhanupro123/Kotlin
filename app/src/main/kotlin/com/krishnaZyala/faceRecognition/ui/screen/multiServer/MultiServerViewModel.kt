package com.krishnaZyala.faceRecognition.ui.screen.multiServer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class MultiServerViewModel(app: Application) : AndroidViewModel(app) {

    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices: StateFlow<List<DeviceInfo>> = _devices

    private var signalingSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val SIGNALING_URL = "ws://192.168.0.149:8080"

    init {
        connectSignalingServer()
        startAutoReconnect()
    }

    private fun connectSignalingServer() {
        val request = Request.Builder().url(SIGNALING_URL).build()
        signalingSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                println("✅ Connected to signaling server")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("✅ Connected and message ${text}")
                if (text.startsWith("DEVICE:")) {
                    val parts = text.removePrefix("DEVICE:").split(",")
                    if (parts.size >= 3) {
                        val device = DeviceInfo(parts[0], parts[1], parts[2])
                        addOrUpdateDevice(device)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                println("❌ Signaling server connection failed: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("⚠️ Signaling server closed: $reason")
            }
        })
    }

    private fun addOrUpdateDevice(device: DeviceInfo) {
        _devices.value = _devices.value.toMutableList().apply {
            val index = indexOfFirst { it.id == device.id }
            if (index >= 0) this[index] = device else add(device)
        }
    }

    fun connectToDevice(deviceId: String) {
        _devices.value = _devices.value.map {
            if (it.id == deviceId) it.copy(isConnected = true) else it
        }
        // TODO: Start WebSocket for video/audio here
    }

    private fun startAutoReconnect() {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                if (signalingSocket == null) {
                    println("🔄 Attempting reconnect to signaling server...")
                    connectSignalingServer()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reconnectJob?.cancel()
        signalingSocket?.close(1000, "App closed")
    }
}
