package com.bhanupro.faceRecognition.ui.screen.Init

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.*
import android.os.Build
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.*
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.concurrent.thread

data class RemoteStream(val senderId: String, val bitmap: Bitmap)

class MultiStreamViewModel : ViewModel() {

    private val _streams = MutableStateFlow<List<RemoteStream>>(emptyList())
    val streams: StateFlow<List<RemoteStream>> = _streams

    private val _selfBitmap = MutableStateFlow<Bitmap?>(null)
    val selfBitmap: StateFlow<Bitmap?> = _selfBitmap
    private val _focusedStreamId = MutableStateFlow<String?>(null)
    val focusedStreamId: StateFlow<String?> = _focusedStreamId

    fun focusStream(senderId: String) {
        _focusedStreamId.value = senderId
    }

    fun clearFocus() {
        _focusedStreamId.value = null
    }

    private val videoClient = OkHttpClient()
    private val audioClient = OkHttpClient()
    private var videoSocket: WebSocket? = null
    private var audioSocket: WebSocket? = null

    private var audioTrack: AudioTrack? = null
    private val deviceId = "Bhanu".padEnd(20) // Replace with hardcoded name
    private val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
    private val fullId = "$deviceId-$deviceModel".padEnd(60)

    companion object {
        const val VIDEO_URL = "ws://192.168.0.149:8765"
        const val AUDIO_URL = "ws://192.168.0.149:8766"
        const val JPEG_QUALITY = 50
        const val HEADER_LENGTH = 60 // id+device name
    }

    private val _isAudioMuted = MutableStateFlow(true)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted
    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled

    init {
        connectSockets()
    }

    fun toggleMute() {
        _isAudioMuted.value = !_isAudioMuted.value
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
    }

    private fun connectSockets() {
        videoSocket = videoClient.newWebSocket(
            Request.Builder().url(VIDEO_URL).build(),
            object : WebSocketListener() {
                override fun onMessage(ws: WebSocket, bytes: okio.ByteString) {
                    val data = bytes.toByteArray()
                    if (data.size <= HEADER_LENGTH) return

                    val senderId = String(data.sliceArray(0 until HEADER_LENGTH)).trim()
                    if (senderId == fullId.trim()) return

                    val currentFocus = _focusedStreamId.value
                    if (currentFocus != null && senderId != currentFocus) return


                    val imageBytes = data.sliceArray(HEADER_LENGTH until data.size)

                    print(imageBytes)
                    val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                    _streams.update { current ->
                        val mutable = current.toMutableList()
                        val index = mutable.indexOfFirst { it.senderId == senderId }
                        if (index >= 0) {
                            mutable[index] = RemoteStream(senderId, bmp)
                        } else {
                            mutable.add(RemoteStream(senderId, bmp))
                        }
                        mutable
                    }

                }
            })

        audioSocket = audioClient.newWebSocket(
            Request.Builder().url(AUDIO_URL).build(),
            object : WebSocketListener() {
                override fun onMessage(ws: WebSocket, bytes: okio.ByteString) {
                    val data = bytes.toByteArray()
                    if (data.size <= HEADER_LENGTH) return

                    val senderId = String(data.sliceArray(0 until HEADER_LENGTH)).trim()
                    if (senderId == fullId.trim()) return

                    val audioData = data.sliceArray(HEADER_LENGTH until data.size)
                    playAudio(audioData)
                }
            })
    }

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
                val jpeg = yuvToJpeg(image)
                jpeg?.let {
                    val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                    _selfBitmap.value = bmp

                    if (_isVideoEnabled.value) {
                        val data = fullId.toByteArray() + it
                        videoSocket?.send(data.toByteString())
                    }
                }
                image.close()
            }

            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
            provider.unbindAll()
            provider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, selector, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
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

    fun startSendingAudio(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
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
                if (read > 0 && !_isAudioMuted.value) {
                    val data = fullId.toByteArray() + buffer.copyOf(read)
                    audioSocket?.send(data.toByteString())
                }
            }
        }
    }

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
        }
        audioTrack?.write(data, 0, data.size)
    }
    fun clearAll()
    {
        videoSocket?.close(1000, null)
        audioSocket?.close(1000, null)
        audioTrack?.release()
    }
    override fun onCleared() {
    clearAll()
        super.onCleared()
    }
}
