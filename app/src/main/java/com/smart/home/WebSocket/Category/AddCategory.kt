package com.smart.home.WebSocket.Category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.Category
import com.smart.home.SharedViewModel
import WebSocketService
import com.google.gson.GsonBuilder
import com.smart.home.Devices.IconGrid


@Composable
fun AddRoom(webSocketService : WebSocketService, navController: NavController, sharedViewModel: SharedViewModel) {

    Box(modifier = Modifier.fillMaxSize().background(Color.White))
    {
         AddCategoryScreen (onAddCategory = {
             navController.popBackStack()
            webSocketService.sendMessage(
                "AddRoom"+GsonBuilder().create().toJson(it).toString())
         })

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(onAddCategory: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    var moreData by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("Favorite") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add New Room",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            value = moreData,
            onValueChange = { moreData = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        IconGrid(selectedIcon = icon, onIconSelected = {
           icon=it
        })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (categoryName.isNotBlank()&&moreData.isNotBlank()) {

                    onAddCategory(Category(
                        name = categoryName,
                        description = moreData,
                        icon = icon,
                        devices =  mutableListOf()
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text(text = "Add Room")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

