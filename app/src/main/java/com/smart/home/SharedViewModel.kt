package com.smart.home

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    // StateFlow for managing the message state
    private val _message = MutableStateFlow("Initial Message")
    val message: StateFlow<String> get() = _message

    // Function to update the message
    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }
}



