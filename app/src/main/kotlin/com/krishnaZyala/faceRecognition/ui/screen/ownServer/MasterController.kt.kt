package com.krishnaZyala.faceRecognition.ui.screen.ownServer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.krishnaZyala.faceRecognition.app.SnackbarManager
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.newDevice.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.concurrent.thread

// Keep your Device_Info and ViewModel definitions outside or above; included here for completeness
data class Device_Info(val ip: String, val name: String)

class MasterController(
    private val context: Context,
    private val viewModel: Multiple_Stream_ViewModel
) {
    companion object {
        private const val TAG_VIDEO = "VideoServer"
        private const val TAG_AUDIO = "AudioServer"
        private const val TAG_SIGNAL = "Signaling"
        private const val VIDEO_PORT = 8081
        private const val AUDIO_PORT = 8082
    }

    // --- networking / signaling ---
    private val client = OkHttpClient.Builder()
        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

    private val ip = getWifiIpAddress(context) // implement elsewhere
    private var signalWebSocket: WebSocket? = null
    private val reconnectDelay = 5000L
    private var reconnectJob: Job? = null

    // --- camera ---
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector? = null
    private var preview: Preview? = null
    private var analysis: ImageAnalysis? = null
    private var cameraExecutor = Executors.newSingleThreadExecutor()
    private val deviceId = "Bhanu"
    // --- audio ---
    private var audioRecorder: AudioRecord? = null
    private var audioThread: Thread? = null
    @Volatile private var isRecording = false
    @Volatile private var isCamStreaming = false

    // --- servers (nullable, recreated when needed) ---
    private var videoServer: WebSocketServer? = null
    private var audioServer: WebSocketServer? = null

    // --- helper UI/log ---
    private fun uiToast(msg: String) {
        CoroutineScope(Dispatchers.Main).launch { SnackbarManager.showMessage(msg) }
    }
    private fun logD(tag: String, msg: String) = Log.d(tag, msg)
    private fun logI(tag: String, msg: String) = Log.i(tag, msg)
    private fun logE(tag: String, msg: String) = Log.e(tag, msg)

    // PUBLIC API
    fun startServers() {
        // If already created, skip — this prevents "can only be started once"
        if (videoServer == null) {
            videoServer = createVideoServer()
            try {
                videoServer?.start()
                logI(TAG_VIDEO, "startServers: videoServer.start() called")
            } catch (e: Exception) {
                logE(TAG_VIDEO, "Failed to start videoServer: ${e.message}")
                uiToast("Video server start failed: ${e.message}")
                // cleanup
                try { videoServer?.stop() } catch (_: Exception) {}
                videoServer = null
            }
        } else {
            logI(TAG_VIDEO, "startServers: videoServer already exists — skipping start")
        }

        if (audioServer == null) {
            audioServer = createAudioServer()
            try {
                audioServer?.start()
                logI(TAG_AUDIO, "startServers: audioServer.start() called")
            } catch (e: Exception) {
                logE(TAG_AUDIO, "Failed to start audioServer: ${e.message}")
                uiToast("Audio server start failed: ${e.message}")
                try { audioServer?.stop() } catch (_: Exception) {}
                audioServer = null
            }
        } else {
            logI(TAG_AUDIO, "startServers: audioServer already exists — skipping start")
        }
    }

    fun stopServers() {
        // stop video server then nullify
        try {
            videoServer?.let {
                try { it.stop(1000) } catch (e: Exception) { logE(TAG_VIDEO, "stop() exception: ${e.message}") }
            }
        } finally {
            videoServer = null
        }

        // stop audio server then nullify
        try {
            audioServer?.let {
                try { it.stop(1000) } catch (e: Exception) { logE(TAG_AUDIO, "stop() exception: ${e.message}") }
            }
        } finally {
            audioServer = null
        }

        // ensure audio resources freed
        stopSendingAudio()
        // ensure camera stopped
        stopCamera()
    }

    fun stopAll() {
        // stop signaling socket too
        try { signalWebSocket?.close(1000, "stopAll") } catch (_: Exception) {}
        signalWebSocket = null

        stopServers()
    }

    // -----------------------
    // Server factories
    // -----------------------
    private fun createVideoServer(): WebSocketServer {
        return object : WebSocketServer(InetSocketAddress(VIDEO_PORT)) {
            init {
                // Allow quick restart without waiting for OS to free the port
                isReuseAddr = true
            }

            private val connections = mutableSetOf<org.java_websocket.WebSocket>()

            override fun onOpen(conn: org.java_websocket.WebSocket, handshake: ClientHandshake) {
                logI(TAG_VIDEO, "onOpen ${conn.remoteSocketAddress}")
                connections.add(conn)
                uiToast("Video client connected: ${connections.size}")
                startCamera()
            }

            override fun onClose(conn: org.java_websocket.WebSocket, code: Int, reason: String, remote: Boolean) {
                logI(TAG_VIDEO, "onClose ${conn.remoteSocketAddress} reason=$reason")
                connections.remove(conn)
                uiToast("Video client disconnected: ${connections.size}")

                if (connections.isEmpty()) {
                    stopCamera()
                    viewModel.setVideoChanged(ConnectionState.AVAILABLE)
                    logI(TAG_VIDEO, "No clients connected — stopping video server")
                    CoroutineScope(Dispatchers.IO).launch {
                        stopSafe()
                    }
                }
            }

            override fun onMessage(conn: org.java_websocket.WebSocket?, message: ByteBuffer?) {
                // Expect binary video frames — handle here if needed
            }

            override fun onMessage(conn: org.java_websocket.WebSocket, message: String) {
                // Handle text messages if needed
            }

            override fun onError(conn: org.java_websocket.WebSocket?, ex: Exception) {
                logE(TAG_VIDEO, "Error: ${ex.message}")
            }

            override fun onStart() {
                logI(TAG_VIDEO, "onStart listening on port $address")
                uiToast("Video server started on port ${address.port}")
                viewModel.setVideoChanged(ConnectionState.CONNECTED)
            }

            override fun stop() {
                connections.clear()
                logI(TAG_VIDEO, "videoServer.stop() finished")
                viewModel.setVideoChanged(ConnectionState.AVAILABLE)
                CoroutineScope(Dispatchers.IO).launch {
                    stopCamera()
                    stopSafe()
                }
            }

            private suspend fun stopSafe() {
                try {
                    this@MasterController.videoServer?.stop(1000)
                } catch (_: Exception) {
                } finally {
                    this@MasterController.videoServer = null
                }
            }
        }
    }

    private fun createAudioServer(): WebSocketServer {
        return object : WebSocketServer(InetSocketAddress(AUDIO_PORT)) {
            init {
                // Allow quick restart without waiting for OS to free the port
                isReuseAddr = true
            }

            private val connections = mutableSetOf<org.java_websocket.WebSocket>()

            override fun onOpen(conn: org.java_websocket.WebSocket, handshake: ClientHandshake) {
                logI(TAG_AUDIO, "onOpen ${conn.remoteSocketAddress}")
                connections.add(conn)
                uiToast("Audio client connected: ${connections.size}")
                if (connections.size == 1) {
                    logI(TAG_AUDIO, "First audio client — starting audio capture")
                    startSendingAudio(context)
                }
            }

            override fun onClose(conn: org.java_websocket.WebSocket, code: Int, reason: String, remote: Boolean) {
                logI(TAG_AUDIO, "onClose ${conn.remoteSocketAddress} reason=$reason")
                connections.remove(conn)
                uiToast("Audio client disconnected: ${connections.size}")

                if (connections.isEmpty()) {
                    stopSendingAudio()
                    viewModel.setAudioChanged(ConnectionState.AVAILABLE)
                    logI(TAG_AUDIO, "No clients connected — stopping audio server")
                    CoroutineScope(Dispatchers.IO).launch {
                        stopSafe()
                    }
                }
            }

            override fun onMessage(conn: org.java_websocket.WebSocket?, message: ByteBuffer?) {
                // Handle incoming audio bytes here if needed
            }

            override fun onMessage(conn: org.java_websocket.WebSocket, message: String) {
                logD(TAG_AUDIO, "Text message from ${conn.remoteSocketAddress}: $message")
            }

            override fun onError(conn: org.java_websocket.WebSocket?, ex: Exception) {
                logE(TAG_AUDIO, "onError: ${ex?.message}")
                uiToast("Audio server error: ${ex?.message ?: "unknown"}")
            }

            override fun onStart() {
                logI(TAG_AUDIO, "onStart listening on port $address")
                uiToast("Audio server started on port ${address.port}")
                viewModel.setAudioChanged(ConnectionState.CONNECTED)
            }

            override fun stop() {
                connections.clear()
                logI(TAG_AUDIO, "audioServer.stop() finished")
                viewModel.setAudioChanged(ConnectionState.AVAILABLE)
                CoroutineScope(Dispatchers.IO).launch {
                    stopSendingAudio()
                    stopSafe()
                }
            }

            private suspend fun stopSafe() {
                try {
                    this@MasterController.audioServer?.stop(1000)
                } catch (_: Exception) {
                } finally {
                    this@MasterController.audioServer = null
                }
            }
        }
    }

    // -----------------------
    // Camera helpers
    // -----------------------
    fun startCamera() {
        if(isCamStreaming) return
        isCamStreaming=true
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .build()

                analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis?.setAnalyzer(cameraExecutor) { image ->
                    try {
                        yuvToJpeg(image)?.let { jpeg ->
                            // send to all connected video clients
                            videoServer?.connections?.forEach { conn ->
                                logI(TAG_VIDEO, "Sending................."+conn.isClosed)
                                try { conn.send(jpeg) } catch (_: Exception) {}
                            }
                        }
                    } catch (e: Exception) {
                        logE(TAG_VIDEO, "Analyzer error: ${e.message}")
                    } finally {
                        image.close()
                    }
                }

                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                // bind lifecycle on main thread
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector!!,
                    preview,
                    analysis
                )
                logI(TAG_VIDEO, "Camera bound to lifecycle")
            } catch (e: Exception) {
                logE(TAG_VIDEO, "startCamera failed: ${e.message}")
                uiToast("Camera start failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        isCamStreaming=false
        // shutdown executor safely (new executor needed if restarted)
        try { cameraExecutor.shutdownNow() } catch (_: Exception) {}
        cameraExecutor = Executors.newSingleThreadExecutor()

        // unbindAll must run on main thread
        cameraProvider?.let { provider ->
            if (Looper.myLooper() == Looper.getMainLooper()) {
                try { provider.unbindAll() } catch (e: Exception) { logE(TAG_VIDEO, "unbindAll error: ${e.message}") }
            } else {
                ContextCompat.getMainExecutor(context).execute {
                    try { provider.unbindAll() } catch (e: Exception) { logE(TAG_VIDEO, "unbindAll error (posted): ${e.message}") }
                }
            }
        }
        preview = null
        analysis = null
        cameraSelector = null
        cameraProvider = null
        logI(TAG_VIDEO, "Camera stopped and unbound")
    }

    // -----------------------
    // Audio helpers
    // -----------------------
    fun startSendingAudio(ctx: Context) {
        if (isRecording) {
            logI(TAG_AUDIO, "startSendingAudio: already recording")
            return
        }

        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            uiToast("RECORD_AUDIO permission not granted")
            return
        }

        try {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            audioRecorder?.startRecording()
            isRecording = true

            audioThread = thread(start = true, isDaemon = true) {
                val buf = ByteArray(bufferSize)
                while (isRecording && audioRecorder != null && audioRecorder!!.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = try { audioRecorder!!.read(buf, 0, buf.size) } catch (e: Exception) { -1 }
                    if (read > 0) {
                        val payload = buf.copyOf(read)
                        // broadcast to audio clients
                        audioServer?.connections?.forEach { conn ->
                            try { conn.send(payload) } catch (_: Exception) {}
                        }
                    }
                }
            }
            logI(TAG_AUDIO, "Audio recording started (buffer=$bufferSize)")
        } catch (e: Exception) {
            logE(TAG_AUDIO, "startSendingAudio failed: ${e.message}")
            uiToast("Audio start failed: ${e.message}")
            stopSendingAudio()
        }
    }

    fun stopSendingAudio() {
        isRecording = false
        try { audioThread?.join(500) } catch (e: InterruptedException) { logE(TAG_AUDIO, "join interrupted: ${e.message}") }
        audioThread = null

        try { audioRecorder?.stop() } catch (e: Exception) { /* ignore */ logE(TAG_AUDIO, "stop audioRecorder: ${e.message}") }
        try { audioRecorder?.release() } catch (e: Exception) { logE(TAG_AUDIO, "release audioRecorder: ${e.message}") }
        audioRecorder = null
        logI(TAG_AUDIO, "Audio recording stopped and released")
    }
    // -----------------------
    // YUV -> JPEG util
    // -----------------------
    private fun yuvToJpeg(image: ImageProxy): ByteArray? {
        return try {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 60, out)
            out.toByteArray()
        } catch (e: Exception) {
            logE(TAG_VIDEO, "yuvToJpeg failed: ${e.message}")
            null
        }
    }

    // -----------------------
    // Signaling (okhttp) with safe try/catch
    // -----------------------
    fun start(signalServerUrl: String) { connectSignal(signalServerUrl) }

    private fun connectSignal(url: String) {
        val request = Request.Builder().url(url).build()
        signalWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                try {
                    viewModel.setSignalConnected(true)
                    logI(TAG_SIGNAL, "Signaling open: $ip")
                    val json = """
                        {
                          "id":"$ip",
                          "name":"${getDeviceName()}",
                          "ip":"$ip",
                          "type":"Video/Audio"
                        }
                    """.trimIndent()
                    ws.send(json)
                } catch (e: Exception) {
                    logE(TAG_SIGNAL, "onOpen exception: ${e.message}")
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    logI(TAG_SIGNAL, "onMessage: ${text}")
                    logI(TAG_SIGNAL, "////////////////////////////////////////////////////////\n////////////////////////////////////////////")
                    uiToast(json.optString("type"))

                    when (json.optString("type")) {
                        "new_device" -> {
                            val deviceJson = json.getJSONObject("device")
                            val ip = deviceJson.getString("ip")
                            val name = deviceJson.optString("name", "Unknown")
                            viewModel.addDevice(Device_Info(ip = ip, name = name))
                        }
                        "device_disconnected" -> {
                            val deviceJson = json.getJSONObject("device")
                            val ip = deviceJson.getString("ip")
                            viewModel.removeDeviceByIp(ip)
                        }
                        "broadcast" -> {
                            if (json.optString("action") == "wakeup") {
                                val toIp = json.getString("toIp")
                                if (toIp == ip) {
                                    startServers()
                                    return
                                }
                            }
                        }
                        "hangup" -> stopServers()
                    }
                } catch (e: Exception) {
                    logE(TAG_SIGNAL, "onMessage parse error: ${e.message}")
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                // ignore binary signaling messages or handle if you need
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                logE(TAG_SIGNAL, "onFailure: ${t.message}")
                viewModel.setSignalConnected(false)
                scheduleReconnect(url)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                logI(TAG_SIGNAL, "onClosed: $reason")
                viewModel.setSignalConnected(false)
                scheduleReconnect(url)
            }
        })
    }

    private fun scheduleReconnect(url: String) {
        if (reconnectJob?.isActive == true) return
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(reconnectDelay)
            connectSignal(url)
        }
    }

    fun sendToServer(json: String) {
       signalWebSocket?.send(json)
    }
}

