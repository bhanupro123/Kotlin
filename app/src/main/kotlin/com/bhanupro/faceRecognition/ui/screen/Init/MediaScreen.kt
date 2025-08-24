package com.bhanupro.faceRecognition.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.*
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.concurrent.thread

object MediaStreamer {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    const val AUDIO_FRAME = 1
    const val VIDEO_FRAME = 2
    const val COMMAND_FRAME = 3

    fun connect(serverUrl: String) {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i("WebSocket", "✅ Connected")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ Error: ${t.message}")
            }
        })
    }

    fun sendCommand(command: String) {
        val json = """{"type":"MODE","value":"$command"}"""
        send(COMMAND_FRAME, json.toByteArray())
    }

    private fun send(frameType: Int, payload: ByteArray) {
        val buffer = ByteBuffer.allocate(4 + payload.size)
        buffer.putInt(frameType)
        buffer.put(payload)
        webSocket?.send(buffer.array().toByteString()) // ✅ FIXED
    }

    fun startSendingAudio(context:Context) {
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        val recorder =   AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder.startRecording()

        thread {
            val buffer = ByteArray(bufferSize)
            while (true) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    send(AUDIO_FRAME, buffer.copyOf(read))
                }
            }
        }


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
                sendJpegFrame(image)
                image.close()
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            provider.unbindAll()
            provider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, cameraSelector, preview, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun sendJpegFrame(imageProxy: ImageProxy) {
        val jpeg = yuvToJpeg(imageProxy)
        if (jpeg != null) {
            send(VIDEO_FRAME, jpeg)
        }
    }

    private fun yuvToJpeg(image: ImageProxy): ByteArray? {
        val yuvImage = imageToYuvImage(image) ?: return null
        val stream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height), 80, stream)
        return stream.toByteArray()
    }

    private fun imageToYuvImage(image: ImageProxy): YuvImage? {
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

        return YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    }
}
