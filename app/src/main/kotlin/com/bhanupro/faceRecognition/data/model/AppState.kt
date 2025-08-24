package com.bhanupro.faceRecognition.data.model

import android.app.Activity
import androidx.annotation.Keep
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope

@Keep
data class AppState(
    val activity: Activity,
    val scope: CoroutineScope,
    val host: NavController,
    val snackbar: SnackbarHostState,
    val timestamp: Long = System.currentTimeMillis(),
)
