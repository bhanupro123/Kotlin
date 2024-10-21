package com.smart.home.WebSocket

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WebSocketViewModel : ViewModel() {
    private val _webSocketData = MutableStateFlow<String>("")
    val webSocketData: StateFlow<String> get() = _webSocketData

    fun updateWebSocketData(newData: String) {
        _webSocketData.value = newData
    }

    fun sendMessage(name: String){

    }

    // Example WebSocket message handler


}
