package com.krishnaZyala.faceRecognition.app

import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.krishnaZyala.faceRecognition.ui.navigation.AppHost
import com.krishnaZyala.faceRecognition.ui.navigation.Routes
import com.krishnaZyala.faceRecognition.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import com.krishnaZyala.faceRecognition.lib.LOG
import kotlinx.coroutines.launch

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object SnackbarManager {
    private val scope: CoroutineScope = MainScope()
    private var snackbarHostState: SnackbarHostState? = null

    fun setHostState(state: SnackbarHostState) {
        snackbarHostState = state
    }

    fun showMessage(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarHostState?.let { hostState ->
            scope.launch {
                hostState.showSnackbar(message, duration = duration)
            }
        }
    }
}

object MAIN {
    @HiltAndroidApp
    class HiltApp : Application()

    @AndroidEntryPoint
    class AppActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setContent { AppContent() }
        }

    }

    @Composable
    fun AppContent() = AppTheme(dynamicColors = true, statusBar = true) {
        val snackbarHostState = remember { SnackbarHostState() }

        // Register with the manager
        LaunchedEffect(Unit) {
            SnackbarManager.setHostState(snackbarHostState)
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Surface(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                AppHost("permissions")
            }
        }
    }

}



