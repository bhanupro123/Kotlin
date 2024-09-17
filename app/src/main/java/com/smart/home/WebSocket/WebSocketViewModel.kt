package com.smart.home.WebSocket

import androidx.lifecycle.ViewModel
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketViewModel : ViewModel() {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send("Hello from Android!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Handle the received message
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // Handle error
        }
    }

    // Initialize WebSocket
    fun startWebSocket(url: String) {
        if (webSocket == null) {
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, webSocketListener)
        }
    }

    // Send message via WebSocket
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    // Clean up WebSocket
    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "App is closing")
        client.dispatcher.executorService.shutdown()
    }
}
