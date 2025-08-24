package com.bhanupro.faceRecognition.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.NavHostController
import com.bhanupro.faceRecognition.MyDeviceAdminReceiver
import com.bhanupro.faceRecognition.app.SnackbarManager
import com.bhanupro.faceRecognition.ui.navigation.NavRoutes

@Composable
fun DeviceAdminActivation(
    onActivated: () -> Unit = {}
) {
    val context = LocalContext.current
    val componentName = remember { ComponentName(context, MyDeviceAdminReceiver::class.java) }
    val devicePolicyManager = remember {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (devicePolicyManager.isAdminActive(componentName)) {
            // User accepted device admin activation
            onActivated()
        } else {
            SnackbarManager.showMessage("Please give permission")
        }
    }

    LaunchedEffect(Unit) {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Activate device admin to prevent phone from sleeping")
            }
            launcher.launch(intent)
        } else {
            onActivated()
        }
    }
}


@Composable
fun PermissionsScreen(navigation: NavHostController) {
    val context = LocalContext.current
    val showRetry = remember { mutableStateOf(false) }

    // Define permissions based on Android version
    val allPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
                // No storage permissions needed on Android 13+
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun navigate()
    {
        navigation.navigate(NavRoutes.INIT) {
            popUpTo(0) { inclusive = true }  // clears the whole back stack
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
 navigate()
        } else {
            showRetry.value = true
        }
    }
    fun requestPermissions()
    {
        val notGranted = allPermissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted) {
            permissionLauncher.launch(allPermissions)
        } else {
            navigate()
        }
    }
    DeviceAdminActivation {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            requestPermissions()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (showRetry.value) {
                    "Permissions were denied.\nPlease grant all to proceed."
                } else {
                    "Requesting Permissions..."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showRetry.value) {
                Button(onClick = {
                    showRetry.value = false
                    permissionLauncher.launch(allPermissions)
                }) {
                    Text("Request Again")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Next button, enabled only if no retry needed
            Button(
                onClick = {
                    requestPermissions()
                },
                enabled = !showRetry.value
            ) {
                Text("Next")
            }
        }
    }

}

