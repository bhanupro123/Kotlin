package com.smart.home
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
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
                message = (message!!),
                actionLabel = actionLabel,
                duration = duration
            )
        }
    }
}
