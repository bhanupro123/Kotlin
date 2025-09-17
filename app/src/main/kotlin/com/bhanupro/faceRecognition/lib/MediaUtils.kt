package com.bhanupro.faceRecognition.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.bhanupro.faceRecognition.lib.DeviceTypeStorage.KEY_DEVICE_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

import java.net.Inet4Address
import java.net.InetAddress

suspend fun getPiIPv4(context:Context ): String? {
    return withContext(Dispatchers.IO) {
        try {
            val addresses = InetAddress.getAllByName("raspberrypi.local")
            val ipv4 = addresses.firstOrNull { it is Inet4Address }
            ipv4?.hostAddress
        } catch (e: Exception) {
            e.printStackTrace()
            DeviceIpStorage.getDeviceIp(context)
        }
    }
}
object PinStorage {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_PIN = "user_pin"

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PIN, pin)
            .apply()
    }

    fun getPin(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PIN, null)
    }

    fun hasPin(context: Context): Boolean {
        return getPin(context) != null
    }
}

enum class DeviceType { NORMAL,SECURITY_MAIN}

object DeviceTypeStorage {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_DEVICE_TYPE = "device_type"

    fun saveDeviceType(context: Context, deviceType: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICE_TYPE, deviceType)
            .apply()
    }

    fun getDeviceType(context: Context): String{
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEVICE_TYPE, null) ?: ""
    }
}
object DeviceIdManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    fun getOrCreateId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return "!"+id+"!"
    }
}

object DeviceIpStorage {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_DEVICE_IP = "device_ip"
    fun saveDeviceIp(context: Context, config: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICE_IP, config)
            .apply()
    }

    fun getDeviceIp(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEVICE_IP, null) ?: "192.168.0.149"
    }
}
object MediaUtils {
    val ImageProxy.bitmap: Result<Bitmap?>
        get(): Result<Bitmap?> = runCatching {
            val yBuffer = planes[0].buffer // Y
            val vuBuffer = planes[2].buffer // VU

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)

            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap.rotate(imageInfo.rotationDegrees.toFloat()).getOrNull()
        }.onFailure { LOG.e(it, it.message) }


    fun Bitmap.rotate(rotation: Float): Result<Bitmap> = runCatching {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }.onFailure { LOG.e(it, it.message) }

    fun Bitmap.flip(vertical: Boolean = false, horizontal: Boolean = false): Result<Bitmap> = runCatching {
        val matrix = Matrix()
        if (vertical) matrix.postScale(1f, -1f)
        if (horizontal) matrix.postScale(-1f, 1f)
        Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }.onFailure { LOG.e(it, it.message) }

    fun Bitmap.crop(left: Int, top: Int, width: Int, height: Int): Result<Bitmap> = runCatching {
        Bitmap.createBitmap(this, left, top, width, height)
    }.onFailure { LOG.e(it, it.message) }
}