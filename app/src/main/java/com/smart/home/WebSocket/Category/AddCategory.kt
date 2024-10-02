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


@Composable
fun AddRoom(navController: NavController,sharedViewModel: SharedViewModel) {
    val globalData=sharedViewModel.globalviewmodal?.value?.globalViewModelData?.value
    Box(modifier = Modifier.fillMaxSize().background(Color.White))
    {
         AddCategoryScreen (onAddCategory = {it->
             globalData?.categories?.add(it)
             navController.popBackStack()
         })
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(onAddCategory: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add New Category",
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
            label = { Text("Id") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            value = categoryId,
            onValueChange = { categoryId = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (categoryName.isNotBlank()&&categoryId.isNotBlank()&&categoryId.length>=10) {
                    onAddCategory(Category(name = categoryName,
                        id = categoryId,
                         devices = emptyList()
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text(text = "Add Category")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

