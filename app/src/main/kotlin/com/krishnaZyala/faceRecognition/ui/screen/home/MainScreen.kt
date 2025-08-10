package com.krishnaZyala.faceRecognition.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.krishnaZyala.faceRecognition.ui.navigation.HomeHost
import com.krishnaZyala.faceRecognition.data.model.AppState
import com.krishnaZyala.faceRecognition.ui.screen.home.HomeScreenState.Companion.defaultBottomBarItem

@Composable
fun MainScreen(appState: AppState, vm: HomeViewModel = hiltViewModel()) {
    val home: NavHostController = rememberNavController()
    val state: HomeScreenState by remember(vm.state) { vm.state }

    DisposableEffect(appState, home) {
        vm.onCompose(appState, home)
        onDispose { vm.onDispose() }
    }

    Text("bhanu")

}
