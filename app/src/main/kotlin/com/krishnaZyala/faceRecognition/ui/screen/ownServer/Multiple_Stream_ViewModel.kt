package com.krishnaZyala.faceRecognition.ui.screen.ownServer

import androidx.lifecycle.ViewModel
import com.krishnaZyala.faceRecognition.ui.screen.ownServer.newDevice.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Multiple_Stream_ViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<Device_Info>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _signalConnected = MutableStateFlow(false)
    private val _videoServerStatus= MutableStateFlow(ConnectionState.AVAILABLE)
    private val _audioServerStatus= MutableStateFlow(ConnectionState.AVAILABLE)
    val signalConnected = _signalConnected.asStateFlow()

    val videoConnected = _videoServerStatus.asStateFlow()

    val audioConnected = _audioServerStatus.asStateFlow()

    fun addDevice(device: Device_Info) {
        if (_devices.value.any { it.ip == device.ip }) return
        _devices.value = _devices.value + device
    }

    fun removeDeviceByIp(ip: String) {
        _devices.value = _devices.value.filterNot { it.ip == ip }
    }

    fun setSignalConnected(connected: Boolean) {
        _signalConnected.value = connected
        if (!connected) _devices.value = emptyList()
    }
    fun setVideoChanged(connected: ConnectionState) {
        _videoServerStatus.value = connected
    }
    fun setAudioChanged(connected: ConnectionState) {
        _audioServerStatus.value = connected
    }
}
