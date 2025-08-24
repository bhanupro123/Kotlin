package com.bhanupro.faceRecognition.ui.screen.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bhanupro.faceRecognition.data.model.AppState

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
