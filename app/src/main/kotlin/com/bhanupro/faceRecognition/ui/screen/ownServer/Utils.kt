package com.bhanupro.faceRecognition.ui.screen.ownServer

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

fun getWifiIpAddress(context: Context): String? {
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ip = wm.connectionInfo.ipAddress
    return if (ip != 0) {
        // Convert little-endian to big-endian if needed
        val ipAddress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
        ipAddress
    } else {
        null
    }
}

private val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
fun getDeviceName():String{
    return deviceName
}