//package com.notifii.lockers
//
//import android.os.Parcelable
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.runtime.Composable
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.navArgument
//import com.google.gson.Gson
//import com.google.gson.GsonBuilder
//import com.notifii.lockers.Global.GViewModel
//import com.notifii.lockers.Global.GlobalLoader
//import com.notifii.lockers.Global.ScreenOne
//import com.notifii.lockers.Global.ScreenTwo
//import com.notifii.lockers.Navigation.Delivery.LockerHeatMap
//import com.notifii.lockers.Navigation.Delivery.ParcelBasicInfo
//import com.notifii.lockers.Navigation.Delivery.ParcelBasicInfoHolder
//import com.notifii.lockers.Navigation.Delivery.ParcelBasicInfoViewModel
//import com.notifii.lockers.Navigation.HomeScreen
//import com.notifii.lockers.Utils.StringConstants
//import com.notifii.lockers.Navigation.PINValidation
//import com.notifii.lockers.Network.APIS.Login.RecipientItem
//
//
//@Composable
//fun MyAppNavigation(gViewModel: GViewModel) {
//    val navController = androidx.navigation.compose.rememberNavController()
//    Scaffold(
//        snackbarHost = {
//            SnackbarHost(hostState = SnackbarManager.snackbarHostState)
//        }
//    ) { paddingValues ->
//        // Set up the NavHost with navigation graph
//        NavHost(navController = navController, startDestination = StringConstants.SPLASH) {
//            composable(StringConstants.PARCEL_BASIC_INFO) {
//                ParcelBasicInfo( navController = navController, gViewModel =gViewModel)
//            }
//            composable(StringConstants.PIN_VALIDATION+"/{routeType}")
//            {backStackEntry->
//                val routeType = backStackEntry.arguments?.getString("routeType","")
//                PINValidation( navController = navController,viewModel=gViewModel,
//                    routeType = if(routeType!=null) routeType else StringConstants.DELIVER_PACKAGES)
//            }
//            composable(StringConstants.HOME) {
//                HomeScreen(navController = navController, gViewModel = gViewModel)
//            }
//            composable(StringConstants.LOCKER_HEAT_MAP+"/{recipientItem}",
//                arguments = listOf(
//                    navArgument("recipientItem") {
//                        defaultValue = ""
//                        type = NavType.StringType
//                    }
//                )) {backStackEntry->
//                val gson: Gson = GsonBuilder().create()
//                val userJson = backStackEntry.arguments?.getString("recipientItem")
//                    LockerHeatMap(navController = navController, parcelBasicInfoHolder =gson.fromJson(userJson, ParcelBasicInfoHolder::class.java), gViewModel = gViewModel)
//
////                val recipientItem = backStackEntry.arguments?.getParcelable<ParcelBasicInfoViewModel>("recipient_item")
////                val parcelBasicInfoViewModel: ParcelBasicInfoViewModel = viewModel()
////                parcelBasicInfoViewModel.setRecipientItem(recipientItem)
////                    LockerHeatMap(navController = navController, gViewModel = gViewModel, parcelBasicInfoViewModel = parcelBasicInfoViewModel)
//
//            }
//
//            composable( StringConstants.LOGIN) {
//                LogInScreen( navController = navController,viewModel=gViewModel)
//            }
//            composable(StringConstants.SPLASH) {
//                SplashScreen( navController = navController,viewModel=gViewModel)
//            }
//            composable("1") {
//                ScreenOne( navController = navController,viewModel=gViewModel)
//            }
//            composable("2") {
//                ScreenTwo( viewModel=gViewModel)
//            }
//            composable("adaptive") {
//                val adaptiveUIScreen = AdaptiveUIScreen()
//                adaptiveUIScreen.AdaptiveUI()
//            }
//        }
//        GlobalLoader(gViewModel)
//    }
//}
