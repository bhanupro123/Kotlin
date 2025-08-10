package com.krishnaZyala.faceRecognition.ui.screen.Init

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.*
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import android.util.Base64
import androidx.lifecycle.ViewModelProvider


private fun getOrCreateDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val storedId = prefs.getString("device_id", null)

    return if (storedId != null) {
        storedId
    } else {
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", newId).apply()
        newId
    }
}

class DualStreamViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DualStreamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DualStreamViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DualStreamViewModel(private val context: Context) : ViewModel() {

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap

    private val videoClient = OkHttpClient()
    private val audioClient = OkHttpClient()
    private var videoSocket: WebSocket? = null
    private var audioSocket: WebSocket? = null

    private var audioTrack: AudioTrack? = null
    private val deviceId = getOrCreateDeviceId(context)

    // Mute/Video toggle

    companion object {
        const val VIDEO_URL = "ws://192.168.0.149:8765"
        const val AUDIO_URL = "ws://192.168.0.149:8766"
        const val JPEG_QUALITY = 50
        const val DEVICE_ID_LENGTH = 36 // UUID length
    }

    init {
        connectSockets()
    }

    private val _isAudioMuted = MutableStateFlow(false)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled

    fun toggleMute() {
        _isAudioMuted.value = !_isAudioMuted.value
        Log.i("AudioToggle", if (_isAudioMuted.value) "🔇 Muted" else "🔊 Unmuted")
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
        Log.i("VideoToggle", if (_isVideoEnabled.value) "📷 On" else "📷 Off")
    }

    private fun connectSockets() {

        videoSocket = videoClient.newWebSocket(
            Request.Builder().url(VIDEO_URL).build(),
            object : WebSocketListener() {
                override fun onMessage(ws: WebSocket, bytes: okio.ByteString) {
                    val data = bytes.toByteArray()
                    if (data.size <= DEVICE_ID_LENGTH) return

                    val senderId = String(data.sliceArray(0 until DEVICE_ID_LENGTH))
                    if (senderId == deviceId) return // Skip self

                    val imageBytes = data.sliceArray(DEVICE_ID_LENGTH until data.size)
                    val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    println("QWERTY$deviceId")
                    val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    _bitmap.value = bmp
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e("VideoSocket", "❌ ${t.localizedMessage}")
                }
            })

        audioSocket = audioClient.newWebSocket(
            Request.Builder().url(AUDIO_URL).build(),
            object : WebSocketListener() {
                override fun onMessage(ws: WebSocket, bytes: okio.ByteString) {
                    val data = bytes.toByteArray()
                    if (data.size <= DEVICE_ID_LENGTH) return

                    val senderId = String(data.sliceArray(0 until DEVICE_ID_LENGTH))
                    if (senderId == deviceId) return // Skip self

                    val audioData = data.sliceArray(DEVICE_ID_LENGTH until data.size)
                    playAudio(audioData)
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e("AudioSocket", "❌ ${t.localizedMessage}")
                }
            })
    }

    // ========== VIDEO ==========
    fun startCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val executor = Executors.newSingleThreadExecutor()
            analysis.setAnalyzer(executor) { image ->
                if (_isVideoEnabled.value) sendJpeg(image)
                image.close()
            }

            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
            provider.unbindAll()
            provider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, selector, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun sendJpeg(imageProxy: ImageProxy) {
        val jpeg = yuvToJpeg(imageProxy) ?: return
        val data = deviceId.toByteArray() + jpeg
        videoSocket?.send(data.toByteString())
    }

    private fun yuvToJpeg(image: ImageProxy): ByteArray? {
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
        val stream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), JPEG_QUALITY, stream)
        return stream.toByteArray()
    }

    // ========== AUDIO ==========
    fun startSendingAudio(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Audio", "Permission not granted")
            return
        }

        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder.startRecording()

        thread(start = true, isDaemon = true) {
            val buffer = ByteArray(bufferSize)
            while (true) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0 && !isAudioMuted.value) {
                    val data = deviceId.toByteArray() + buffer.copyOf(read)
                    println("BHANU Audio sending...........")
                    audioSocket?.send(data.toByteString())
                }
            }
        }
    }

    private fun playAudio(data: ByteArray) {
        val sampleRate = 16000
        if (audioTrack == null) {
            println("BHANU Audio Playing......................")
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
        }
        audioTrack?.write(data, 0, data.size)
    }

    override fun onCleared() {
        videoSocket?.close(1000, null)
        audioSocket?.close(1000, null)
        audioTrack?.release()
        super.onCleared()
    }
}
