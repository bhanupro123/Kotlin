package com.bhanupro.faceRecognition.ui.screen.dashBoard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bhanupro.faceRecognition.lib.PinStorage

@Composable
fun PinDialog(
    context: Context,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val hasPin = remember { PinStorage.hasPin(context) }
    val title = if (hasPin) "Enter PIN" else "Create PIN"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pin = it
                    },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )

                if (!hasPin) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPin = it
                        },
                        label = { Text("Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                }

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (hasPin) {
                    if (pin == PinStorage.getPin(context)) {
                        error = ""
                        onSuccess()
                        onDismiss()
                    } else {
                        error = "Incorrect PIN"
                    }
                } else {
                    if (pin.length == 4 && pin == confirmPin) {
                        PinStorage.savePin(context, pin)
                        error = ""
                        onSuccess()
                        onDismiss()
                    } else {
                        error = "PINs do not match or are invalid"
                    }
                }
            }) {
                Text(if (hasPin) "Login" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
