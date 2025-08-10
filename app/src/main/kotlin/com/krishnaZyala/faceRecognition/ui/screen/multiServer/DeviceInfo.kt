package com.krishnaZyala.faceRecognition.ui.screen.multiServer



data class DeviceInfo(
    val id: String,
    val name: String,
    val ip: String,
    val isConnected: Boolean = false
)
