package com.smart.home.WebSocket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import okhttp3.*

class WebSocketService : Service() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send("Connected to server")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Handle incoming message
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, "Service stopping")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // Handle error
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = Request.Builder().url("ws://your-websocket-url").build()
        webSocket = client.newWebSocket(request, webSocketListener)
        return START_STICKY
    }

    override fun onDestroy() {
        webSocket?.close(1000, "Service stopped")
        client.dispatcher.executorService.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
