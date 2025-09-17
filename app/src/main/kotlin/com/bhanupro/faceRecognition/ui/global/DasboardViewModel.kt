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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

data class DashboardUiState(
    val showPinDialog: Boolean = false,
    val showLoginDialog: Boolean = false,
    val showConnectionDialog: Boolean = true,
    val showCallDialog: Boolean = false,
    val showSheet: Boolean = false,
    val isItForUnLock: Boolean = false,
    val showDevicesList:Boolean =false
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun setShowLoginDialog(show: Boolean, unlock: Boolean = false) {
        _uiState.update { it.copy(showLoginDialog = show, isItForUnLock = unlock) }
    }

    fun setShowPinDialog(show: Boolean) {
        _uiState.update { it.copy(showPinDialog = show) }
    }

    fun setShowConnectionDialog(show: Boolean) {
        _uiState.update { it.copy(showConnectionDialog = show) }
    }

    fun setShowSheet(show: Boolean) {
        _uiState.update { it.copy(showSheet = show) }
    }

    fun setShowCallDialog(show: Boolean) {
        _uiState.update { it.copy(showCallDialog = show) }
    }
    fun unlockDoor() {
        SnackbarManager.showMessage("Unlocked")
    }
    fun setShowDevicesList(show: Boolean) {
        _uiState.update { it.copy(showDevicesList = show) }
    }


}
