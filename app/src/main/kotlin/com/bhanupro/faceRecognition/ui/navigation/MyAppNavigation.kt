package com.bhanupro.faceRecognition.ui.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bhanupro.faceRecognition.ui.global.GlobalViewModel
import com.bhanupro.faceRecognition.ui.screen.home.InitScreen

@Composable
fun MyAppNavigation(gViewModel: GlobalViewModel, navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "main"
        )
        {
            composable(NavRoutes.INIT) {
                InitScreen(navigation = navController)
            }

        }
    }
}
