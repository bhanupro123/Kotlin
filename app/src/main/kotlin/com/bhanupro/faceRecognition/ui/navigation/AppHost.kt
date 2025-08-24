package com.bhanupro.faceRecognition.ui.navigation

import android.app.Activity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bhanupro.faceRecognition.data.model.AppState
import com.bhanupro.faceRecognition.ui.screen.dashBoard.DoorControlDashboard
import com.bhanupro.faceRecognition.ui.screen.multiServer.MultiServerScreen
import com.bhanupro.faceRecognition.ui.screen.home.AudioStream
import com.bhanupro.faceRecognition.ui.screen.home.HomeScreen
import com.bhanupro.faceRecognition.ui.screen.home.InitScreen
import com.bhanupro.faceRecognition.ui.screen.home.PermissionsScreen
import com.bhanupro.faceRecognition.ui.screen.home.VideoReceiverScreen
import com.bhanupro.faceRecognition.ui.screen.ownServer.OwnServerScreen
import kotlinx.coroutines.CoroutineScope

// --- Add these imports for your global ViewModel and CompositionLocal ---
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.pointerInput
import com.bhanupro.faceRecognition.ui.global.GlobalViewModel
import com.bhanupro.faceRecognition.ui.screen.addFace.AddFaceScreen
import com.bhanupro.faceRecognition.ui.screen.faces.FacesScreen
import com.bhanupro.faceRecognition.ui.screen.recogniseFace.RecogniseFaceScreen


// --- CompositionLocal for GlobalViewModel ---
val LocalGlobalViewModel = staticCompositionLocalOf<GlobalViewModel> {
    error("GlobalViewModel not provided")
}

@Composable
fun AppHost(
    globalViewModel: GlobalViewModel,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    host: NavHostController = rememberNavController(),
    activity: Activity = LocalContext.current as Activity,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    state: AppState = AppState(activity, scope, host, snackbar),
    builder: NavGraphBuilder.() -> Unit = appNavGraphBuilder(state,host),
) {


    CompositionLocalProvider(LocalGlobalViewModel provides globalViewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                           // globalViewModel.resetTimeout()
                        },
                        onPress = {
                            //globalViewModel.resetTimeout()
                            //tryAwaitRelease()
                        }
                    )
                }
        ) {
            NavHost(host, startDestination, modifier, route, builder)
        }
    }
    }

object NavRoutes {
    const val INIT = "splash"
    const val HOME = "home"
    const val RECOGNISE = "recognise"
    const val ADD_FACE = "addFace"
    const val FACES = "faces"
    const val DASHBOARD = "dashboard"
    const val AUDIO_STREAM = "audioStream"
    const val PERMISSIONS = "permissions"
    const val MEDIA = "media"
    const val OWN_SERVER = "ownserver"
    const val MULTI_SERVER = "multiserver"

}

fun appNavGraphBuilder(state: AppState, host: NavHostController): NavGraphBuilder.() -> Unit = {
    composable(NavRoutes.INIT) {
        InitScreen(navigation = host)
    }
    composable(NavRoutes.HOME) {
        HomeScreen(state)
    }
    composable(NavRoutes.RECOGNISE) {
        RecogniseFaceScreen(state, host)
    }
    composable(NavRoutes.ADD_FACE) {
        AddFaceScreen(state, host)
    }
    composable(NavRoutes.FACES) {
        FacesScreen(state, host)
    }
    composable(NavRoutes.DASHBOARD) {
        DoorControlDashboard(state)
    }
    composable(NavRoutes.AUDIO_STREAM) {
        AudioStream(state)
    }
    composable(NavRoutes.PERMISSIONS) {
        PermissionsScreen(host)
    }
    composable(NavRoutes.MEDIA) {
        VideoReceiverScreen()
    }
    composable(NavRoutes.OWN_SERVER) {
        OwnServerScreen()
    }
    composable(NavRoutes.MULTI_SERVER) {
        MultiServerScreen()
    }
}