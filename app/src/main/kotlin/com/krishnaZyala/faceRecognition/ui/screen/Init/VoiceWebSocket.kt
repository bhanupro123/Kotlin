package com.krishnaZyala.faceRecognition.ui.screen.Init

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

open class VoiceWebSocket(private val context: Context, serverUri: URI) : WebSocketClient(serverUri) {

    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var isRunning = false
    private var isMuted = true
    private var applyNoiseThreshold = true // change to false to disable RMS check

    private val bufferSize = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("WebSocket", "✅ Connected")
        startStreaming()
    }

    override fun onMessage(message: String) {
        Log.d("WebSocket", "ℹ️ Ignored text: $message")
    }

    override fun onMessage(bytes: ByteBuffer?) {
        bytes?.let {
            val data = ByteArray(it.remaining())
            it.get(data)
            player?.write(data, 0, data.size)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WebSocket", "❌ Closed: $reason")
        stopStreaming()
    }

    override fun onError(ex: Exception?) {
        Log.e("WebSocket", "⚠️ Error: ${ex?.message}")
        stopStreaming()
    }

    private fun isVoice(buffer: ByteArray, read: Int, threshold: Int = 600): Boolean {
        var sum = 0.0
        var count = 0
        for (i in 0 until read step 2) {
            if (i + 1 < read) {
                val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                sum += sample * sample
                count++
            }
        }
        if (count == 0) return false
        val rms = Math.sqrt(sum / count)
        return rms > threshold
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startStreaming() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("WebSocket", "❌ Missing RECORD_AUDIO permission")
             return
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 🔊 Route output to speaker if available
        audioManager.availableCommunicationDevices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        }?.let {
            if (audioManager.setCommunicationDevice(it)) {
                Log.d("WebSocket", "✅ Speakerphone routing enabled")
            }
        }

        // 🎧 Request voice communication focus
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .build()
        audioManager.requestAudioFocus(focusRequest)

        // 🎙️ Setup AudioRecord
        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val audioSessionId = recorder!!.audioSessionId
        if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(audioSessionId)
        if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(audioSessionId)
        if (AutomaticGainControl.isAvailable()) AutomaticGainControl.create(audioSessionId)

        // 🔊 Setup playback
        player = AudioTrack(
            AudioManager.STREAM_MUSIC,
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        isRunning = true
        recorder?.startRecording()
        player?.play()

        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
            val buffer = ByteArray(bufferSize)
            val threshold = 600

            while (isRunning && isOpen) {
                val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0 && !isMuted) {
                    val shouldSend = if (applyNoiseThreshold) isVoice(buffer, read, threshold) else true
                    if (shouldSend) {
                        send(buffer.copyOf(read))
                    }
                }
            }
        }.start()
    }

    private fun stopStreaming() {
        isRunning = false
        recorder?.stop()
        recorder?.release()
        recorder = null

        player?.stop()
        player?.release()
        player = null
    }

    fun setMute(mute: Boolean) {
        isMuted = mute
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.isMicrophoneMute = mute
        } else {
            @Suppress("DEPRECATION")
            audioManager.setMicrophoneMute(mute)
        }

        Log.d("WebSocket", if (mute) "🔇 Hardware mic muted" else "🎤 Hardware mic unmuted")
    }


    fun isMicMuted(): Boolean {
        return isMuted
    }

    fun setNoiseFilterEnabled(enabled: Boolean) {
        applyNoiseThreshold = enabled
        Log.d("WebSocket", if (enabled) "🎚️ Noise filter ON" else "🎚️ Noise filter OFF")
    }
}

