package com.krishnaZyala.faceRecognition.ui.global

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.krishnaZyala.faceRecognition.MyDeviceAdminReceiver
import com.krishnaZyala.faceRecognition.app.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- User roles enum ---
enum class UserRole { ADMIN, GUEST, MEMBER }

// --- User data class ---
data class UserData(
    val username: String = "Heck",
    val role: UserRole = UserRole.GUEST,
    val id:String=""
)

// --- GlobalViewModel holding user info ---
class GlobalViewModel( private val context: Context) : ViewModel() {
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()
    private val componentName =   ComponentName(context, MyDeviceAdminReceiver::class.java)
    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    fun setUser(user: UserData) {
        _userData.value = user
    }
    private var timeoutJob: Job? = null
    private val timeoutMillis = 60_000L // e.g., 60 seconds idle timeout

    fun resetTimeout() {
        SnackbarManager.showMessage("Reseting")
        timeoutJob?.cancel()
        timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMillis)
        lockAndSleep()
        //onTimeout()
        }
    }


    fun cancelTimeout() {
        timeoutJob?.cancel()
    }

    fun lockAndSleep() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()  // Lock device immediately (turn off screen)
        }
        wakeUpScreenForSeconds(2000L) // Wake screen for 3 seconds after 5 seconds (use separate delay for that)
    }

    private fun wakeUpScreenForSeconds(duration: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000L) // wait 5 seconds before wakeup
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "FaceRecognitionApp:WakeLock"
            )
            wakeLock.acquire(duration)
        }
    }
}