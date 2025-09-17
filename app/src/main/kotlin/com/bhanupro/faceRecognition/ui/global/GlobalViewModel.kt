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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.bhanupro.faceRecognition.ui.screen.ownServer.Device_Info
import com.bhanupro.faceRecognition.ui.screen.ownServer.MasterController
import com.bhanupro.faceRecognition.ui.screen.ownServer.newDevice.ConnectionState

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
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
data class Device(
    val id: String,
    val name: String,
    val desc: String,
    val catogery: String,
    val deviceType: String,
    val animation: String,
    var brightness: Int,
    var enabled: Boolean,
    val startTime: String,
    val endTime: String,
    var lastStatus: Boolean,
    val lastTriggerAt: String,
    var mode: String,
    var more: String,
    var sensors: String,
    var status: Boolean,
    var temp: String,
    var timeout: String,
    var toggled: Boolean
)

// --- User data ---
data class UserData(
    val username: String = "Heck",
    val avatar: Bitmap? = null,
    val role: UserRole? = null,
    val id: String = "",
    val loginMode: String = ""
)

class GlobalViewModel (private val context: Context) : ViewModel() {

    val masterController: MasterController by lazy {
        MasterController(context,this)
    }


    private val _devicesList = MutableStateFlow<List<Device>>(emptyList())

    // public immutable flow
    val devicesList: StateFlow<List<Device>> = _devicesList.asStateFlow()

    fun setDevices(devices: List<Device>) {
        _devicesList.value = devices
    }

    fun clearDevices() {
        _devicesList.value = emptyList()
    }


    companion object {
        const val USB_PERMISSION_ACTION = "com.bhanupro.faceRecognition.USB_PERMISSION"
    }
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
        _devices.value += device
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
    private val _userData = MutableStateFlow(UserData())
    private val _deviceType =
        MutableStateFlow(
            runCatching {
                val saved = DeviceTypeStorage.getDeviceType(context)
                if (saved.isBlank()) DeviceType.SECURITY_MAIN  // default
                else DeviceType.valueOf(saved)
            }.getOrElse { DeviceType.SECURITY_MAIN } // fallback in case of invalid value
        )

    val userData: StateFlow<UserData> = _userData.asStateFlow()
    val deviceType: StateFlow<DeviceType> = _deviceType.asStateFlow()
    private val _wsUrl = MutableStateFlow<String?>("")
    val wsUrl: StateFlow<String?> = _wsUrl
    private val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun sendDeviceUpdate(device: Device) {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(device)
            val json = JSONObject(jsonString)   // convert gson output to JSONObject
            json.put("type", "update_device")
           // sendMessage(json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchWsUrl() {
        val url =wsUrl.value
        if (!url.isNullOrBlank()) {
            masterController.start(url)
            return
        } else {
            SnackbarManager.showMessage("WebSocket URL is null or empty")
        }
//        val ref = Firebase.database
//            .getReference("cloudflare_tunnel/url")
//        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
//            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//                val url = snapshot.getValue<String>()
//
//                if (url != null) {
//                    _wsUrl.value = url
//                    masterController.start(url)
//                }
//                else{
//                    SnackbarManager.showMessage("Firebase url is null")
//                }
//            }
//            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
//                println("Failed to read WS URL: ${error.message}")
//            }
//        })
//

    }

    fun fetchPiIp() {
        viewModelScope.launch {
           val piIp = getPiIPv4(context)
            println(piIp+"QWERTY")
            masterController.start("ws://${piIp}:8080")
        }
    }
    fun setUser(user: UserData,isLogout:Boolean=false) {
        if(isLogout&&deviceType.value==DeviceType.SECURITY_MAIN) {
            clearDevices()
        }
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


    override fun onCleared() {
        super.onCleared()
        masterController.stopAll() // clean up if you have a stop/close function
    }

    fun startUsbAutoConnect() {
        stopUsbAutoConnect()
        // Register attach/detach receiver
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

    fun stopAll() {
        stopUsbAutoConnect()
        masterController.stopAll()
    }


    fun updateDevice( updates: JSONObject) {

        val id = updates.optString("id")
        val devices = devicesList.value?.toMutableList()

        val index = devices?.indexOfFirst { it.id == id }
        if (index == -1) return  // device not found
        println("onMessage 000  "+id+" "+updates.optString("status"))
        val oldDevice = index?.let { devices.get(it) }

        // Merge updates
        val updatedDevice = oldDevice?.copy(
            status = updates.optBoolean("status", oldDevice.status),
            brightness = updates.optInt("brightness", oldDevice.brightness),
            mode = updates.optString("mode", oldDevice.mode),
            timeout = updates.optString("timeout", oldDevice.timeout),
            toggled = updates.optBoolean("toggled", oldDevice.toggled),
            lastTriggerAt = updates.optString("lastTriggerAt", oldDevice.lastTriggerAt),
            animation = updates.optString("animation", oldDevice.animation),
            startTime = updates.optString("startTime", oldDevice.startTime),
            endTime = updates.optString("endTime", oldDevice.endTime),
            lastStatus = updates.optBoolean("lastStatus", oldDevice.lastStatus),
            more = updates.optString("more", oldDevice.more),
            sensors = updates.optString("sensors", oldDevice.sensors),
            temp = updates.optString("temp", oldDevice.temp)
        )
        if (updatedDevice != null) {
            devices[index] = updatedDevice
            setDevices(devices) // push updated list back to UI
            print("onMessage status  "+devices[index].status)
        }

    }

}
