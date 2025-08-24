package com.bhanupro.faceRecognition.app

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
import androidx.compose.ui.Modifier
import com.bhanupro.faceRecognition.ui.navigation.AppHost
import com.bhanupro.faceRecognition.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.LocalContext
import com.bhanupro.faceRecognition.ui.global.GlobalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

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
        val context = LocalContext.current
        val globalViewModel = remember { GlobalViewModel(context) }
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
                AppHost(globalViewModel,"permissions")
            }
        }
    }

}



