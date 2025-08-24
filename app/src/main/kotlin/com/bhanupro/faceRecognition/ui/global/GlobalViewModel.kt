package com.bhanupro.faceRecognition.ui.global

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.bhanupro.faceRecognition.MyDeviceAdminReceiver
import com.bhanupro.faceRecognition.app.SnackbarManager
import com.bhanupro.faceRecognition.lib.DeviceType
import com.bhanupro.faceRecognition.lib.DeviceTypeStorage
import com.bhanupro.faceRecognition.lib.getPiIPv4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

// --- USB attach/detach receiver ---
class UsbReceiver(
    private val onDeviceConnected: (UsbDevice) -> Unit,
    private val onDeviceDisconnected: (UsbDevice) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Log.d("UsbReceiver", "USB Device Attached: $device")
                device?.let { onDeviceConnected(it) }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Log.d("UsbReceiver", "USB Device Detached: $device")
                device?.let { onDeviceDisconnected(it) }
            }
        }
    }
}

// --- USB permission receiver ---
class UsbPermissionReceiver(
    private val onPermissionGranted: (UsbDevice) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GlobalViewModel.USB_PERMISSION_ACTION) {
            synchronized(this) {
                val device =
                    intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let { onPermissionGranted(it) }
                } else {
                    SnackbarManager.showMessage("USB Permission Denied")
                }
            }
        }
    }
}

// --- User roles ---
enum class UserRole { ADMIN, GUEST, MEMBER }

// --- User data ---
data class UserData(
    val username: String = "Heck",
    val avatar: Bitmap? = null,
    val role: UserRole? = null,
    val id: String = "",
    val loginMode: String = ""
)

class GlobalViewModel(private val context: Context) : ViewModel() {

    companion object {
        const val USB_PERMISSION_ACTION = "com.bhanupro.faceRecognition.USB_PERMISSION"
    }

    private val _userData = MutableStateFlow(UserData())
    private val _deviceType =
        MutableStateFlow(DeviceType.valueOf(DeviceTypeStorage.getDeviceType(context)))
    val userData: StateFlow<UserData> = _userData.asStateFlow()
    val deviceType: StateFlow<DeviceType> = _deviceType.asStateFlow()

    private val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun fetchPiIp() {
        viewModelScope.launch {
           val piIp = getPiIPv4()
            println(piIp+"QWERTY")
        }
    }
    fun setUser(user: UserData) {
        _userData.value = user
    }

    fun setDeviceType(device: DeviceType) {
        _deviceType.value = device
    }

    private var timeoutJob: Job? = null
    private val timeoutMillis = 60_000L

    fun resetTimeout() {
        SnackbarManager.showMessage("Reseting")
        timeoutJob?.cancel()
        timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMillis)
            lockAndSleep()
        }
    }

    fun cancelTimeout() {
        timeoutJob?.cancel()
    }

    fun lockAndSleep() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        }
        wakeUpScreenForSeconds(2000L)
    }

    private fun wakeUpScreenForSeconds(duration: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000L)
            val powerManager =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "FaceRecognitionApp:WakeLock"
            )
            wakeLock.acquire(duration)
        }
    }

    // --- USB stuff ---
    private var usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbPort: UsbSerialPort? = null
    private var autoConnectJob: Job? = null

    private val usbAttachReceiver = UsbReceiver(
        onDeviceConnected = { device ->
            SnackbarManager.showMessage("USB Attached: ${device.deviceName}")
            requestUsbPermission(device)
        },
        onDeviceDisconnected = {
            SnackbarManager.showMessage("USB Disconnected")
            disconnectUsb()
        }
    )

    private val usbPermissionReceiver = UsbPermissionReceiver { device ->
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        driver?.let { connectUsb(it) }
    }

    init {
        // Register attach/detach receiver
        checkAndConnectUsb()
        val attachFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbAttachReceiver, attachFilter)

        // Register permission receiver
        ContextCompat.registerReceiver(
            context,
            usbPermissionReceiver,
            IntentFilter(USB_PERMISSION_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun startUsbAutoConnect() {
        stopUsbAutoConnect()
//        autoConnectJob = CoroutineScope(Dispatchers.IO).launch {
//            while (isActive) {
//                checkAndConnectUsb()
//                delay(2000)
//            }
//        }
    }

    fun stopUsbAutoConnect() {
        autoConnectJob?.cancel()
    }

    private fun checkAndConnectUsb() {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isNotEmpty()) {
            val driver = availableDrivers[0]
            if (!usbManager.hasPermission(driver.device)) {
                requestUsbPermission(driver.device)
            } else {
                connectUsb(driver)
            }
        }
        else{

        }
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(USB_PERMISSION_ACTION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun connectUsb(driver: UsbSerialDriver) {
        try {
            val connection = usbManager.openDevice(driver.device) ?: return
            usbPort = driver.ports[0]
            usbPort?.open(connection)
            usbPort?.setParameters(
                115200,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            SnackbarManager.showMessage("USB Serial Connected")
            listenUsbData()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenUsbData() {
        CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (isActive && usbPort != null) {
                try {
                    val len = usbPort!!.read(buffer, 1000)
                    if (len > 0) {
                        val data = String(buffer, 0, len)
                        Log.d("USB_DATA", "Received: $data")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    disconnectUsb()
                    break
                }
            }
        }
    }

    private fun disconnectUsb() {
        try {
            usbPort?.close()
        } catch (_: IOException) {
        }
        usbPort = null
    }
}
