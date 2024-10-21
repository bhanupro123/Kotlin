package com.smart.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smart.home.Devices.AddDevice
import com.smart.home.Devices.ImportDevice
import com.smart.home.Utils.StringConstants
import com.smart.home.Utils.iconMap
import com.smart.home.WebSocket.Category.AddRoom
import com.smart.home.WebSocket.Category.CategoryViewer
import com.smart.home.WebSocket.Category.MainHall
import WebSocketService
import androidx.compose.ui.platform.LocalContext
import com.smart.home.Devices.PairDevices

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavHost(webSocketClient : WebSocketService,sharedViewModel:SharedViewModel) {
    val navController = rememberNavController()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = SnackbarManager.snackbarHostState)
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = StringConstants.HOME,
        ) {
            composable(StringConstants.HOME) { HomeScreen(navController,sharedViewModel,webSocketClient) }
            composable("second") { SecondScreen() }
            composable(StringConstants.ADDDEVICE) { AddDevice(webSocketClient,navController,sharedViewModel) }
            composable(StringConstants.MAINHALL) { MainHall(webSocketClient,navController,sharedViewModel) }
            composable(StringConstants.AddRoom) { AddRoom(webSocketClient,navController,sharedViewModel) }
            composable( StringConstants.IMPORTDEVICE+"/{category}",
                arguments = listOf(
                    navArgument("category") {
                        defaultValue = ""
                        type = NavType.StringType
                    }
                )) {backStackEntry->
                val gson: Gson = GsonBuilder().create()
                val userJson = backStackEntry.arguments?.getString("category")
                ImportDevice(webSocketClient,navController,sharedViewModel,
                    category = gson.fromJson(userJson, Category::class.java))
            }
            composable( StringConstants.PAIRDEVICE+"/{category}",
                arguments = listOf(
                    navArgument("category") {
                        defaultValue = ""
                        type = NavType.StringType
                    }
                )) {backStackEntry->
                val gson: Gson = GsonBuilder().create()
                val userJson = backStackEntry.arguments?.getString("category")
                PairDevices(webSocketClient,navController,sharedViewModel,
                    category = gson.fromJson(userJson, Category::class.java))
            }
            composable( StringConstants.CATEGORYVIEWER+"/{category}",
                arguments = listOf(
                    navArgument("category") {
                        defaultValue = ""
                        type = NavType.StringType
                    }
                )) {backStackEntry->
                val gson: Gson = GsonBuilder().create()
                val userJson = backStackEntry.arguments?.getString("category")
                CategoryViewer(webSocketClient,navController,sharedViewModel, category = gson.fromJson(userJson, Category::class.java))
            }
        }
    }



}
@Composable
fun GetIconByName(iconName: String) {
    val icon = iconMap[iconName] ?: Icons.Filled.Help
    Icon(imageVector = icon, contentDescription = iconName)
}
