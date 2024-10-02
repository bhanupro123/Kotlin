package com.notifii.lockers


import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import com.notifii.lockers.Utils.handleNetworkError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SnackbarManager {
    val snackbarHostState = SnackbarHostState()
    fun showSnackbar(
        message: String?="UN KNOWN",
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.showSnackbar(
                message = handleNetworkError(message!!),
                actionLabel = actionLabel,
                duration = duration
            )
        }
    }
}
