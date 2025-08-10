package com.krishnaZyala.faceRecognition.ui.screen.ownServer.newDevice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishnaZyala.faceRecognition.app.SnackbarManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

enum class ConnectionState { CONNECTING, CONNECTED,  AVAILABLE }

object WebSocketClientProvider {
    val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
}

class NewDeviceViewmodel : ViewModel() {

    private var videoSocket: WebSocket? = null
    private var audioSocket: WebSocket? = null
    private var audioTrack: AudioTrack? = null
    private var isHangUp: Boolean = false

    var latestFrame by mutableStateOf<Bitmap?>(null)
        private set

    var connectionState by mutableStateOf(ConnectionState.AVAILABLE)
        private set

    private var reconnectAttempts = 0
    private val maxReconnects = 5

    fun startStreaming(ip: String) {
        isHangUp=false
        stopSockets()
        connectionState = ConnectionState.CONNECTING
        reconnectAttempts = 0
        Log.i("NewDeviceVM", "Starting streaming to $ip")
        connectVideo(ip)
        connectAudio(ip)
    }

    /** ---------------- VIDEO ---------------- **/
    private fun connectVideo(ip: String) {
        val request = Request.Builder().url("ws://$ip:8081").build()
        videoSocket = WebSocketClientProvider.client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i("NewDeviceVM", "Video WebSocket connected")
                connectionState = ConnectionState.CONNECTED
                reconnectAttempts = 0
                SnackbarManager.showMessage("Video connected")
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                latestFrame = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.size)
                Log.v("NewDeviceVM", "Video frame received: ${bytes.size} bytes")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("NewDeviceVM", "Video WebSocket closed: $reason")
                connectionState = ConnectionState.AVAILABLE
                if(reason!="byme")
                     handleReconnect(ip)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("NewDeviceVM", "Video WebSocket failure: ${t.message}", t)
                connectionState = ConnectionState.AVAILABLE
                handleReconnect(ip)
            }
        })
    }

    /** ---------------- AUDIO ---------------- **/
    private fun connectAudio(ip: String) {
        val request = Request.Builder().url("ws://$ip:8082").build()
        audioSocket = WebSocketClientProvider.client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                playAudio(bytes.toByteArray())
                Log.v("NewDeviceVM", "Audio packet received: ${bytes.size} bytes")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("NewDeviceVM", "Audio WebSocket closed: $reason")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("NewDeviceVM", "Audio WebSocket failure: ${t.message}", t)
            }
        })
    }

    /** ---------------- RECONNECT ---------------- **/
    private fun handleReconnect(ip: String) {
        if(isHangUp) return
        videoSocket = null
        connectionState = ConnectionState.CONNECTING
        reconnectAttempts++

        Log.w("NewDeviceVM", "Reconnect attempt $reconnectAttempts/$maxReconnects")

        if (reconnectAttempts <= maxReconnects) {
            viewModelScope.launch {
                delay(5000)
                if (connectionState != ConnectionState.CONNECTED) {
                    SnackbarManager.showMessage("Retrying connection ($reconnectAttempts/$maxReconnects)")
                    connectVideo(ip)
                    connectAudio(ip)
                }
            }
        } else {
            connectionState = ConnectionState.AVAILABLE
            SnackbarManager.showMessage("Connection failed after $maxReconnects attempts")
            Log.e("NewDeviceVM", "Max reconnect attempts reached.")
        }
    }
fun exit()
{
    reconnectAttempts=0
    isHangUp=true
    videoSocket?.close(1000,"by me")
    audioSocket?.close(1000,"by me")
}


    /** ---------------- CLEANUP ---------------- **/
    private fun stopSockets() {
        videoSocket?.close(1000, "Closing old video socket")
        audioSocket?.close(1000, "Closing old audio socket")
        videoSocket = null
        audioSocket = null
    }

    override fun onCleared() {
        Log.i("NewDeviceVM", "ViewModel cleared")
        stopSockets()
        audioTrack?.release()
        audioTrack = null
        super.onCleared()
    }

    /** ---------------- AUDIO PLAYBACK ---------------- **/
    private fun playAudio(data: ByteArray) {
        val sampleRate = 16000
        if (audioTrack == null) {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
            Log.i("NewDeviceVM", "AudioTrack initialized and started")
        }
        audioTrack?.write(data, 0, data.size)
    }
}
