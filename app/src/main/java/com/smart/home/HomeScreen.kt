package com.smart.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeMax
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Living
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayForWork
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SingleBed
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.SharedViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RoomPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smart.home.Utils.StringConstants
import WebSocketService
import com.notifii.lockers.Utils.getRandomString

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(navController: NavController, sharedViewModel: SharedViewModel,webSocketClient : WebSocketService) {
    var isDarkMode by remember { mutableStateOf(false) }
    val globalData= sharedViewModel.globalViewModelData.collectAsState()
    val message by sharedViewModel.message.collectAsState()
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1c1c1a)),
        contentAlignment = Alignment.BottomEnd) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row (
                modifier = Modifier
                    .fillMaxWidth().clip(shape = RoundedCornerShape(10.dp))
                    .shadow(2.dp, spotColor = Color.Red) // Apply shadow here
                    .background(Color(0xFF095F59)),
                verticalAlignment = Alignment.CenterVertically, // Center the content vertically
                horizontalArrangement = Arrangement.Start // Align content to the start (left)
            )           {
                Icon (
                    imageVector = Icons.Rounded.Cottage, // Profile icon
                    contentDescription = "Profile Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp), // Padding inside the icon
                    tint = Color.White // Set the icon color
                )

                Spacer(modifier = Modifier.width(12.dp)) // Space between icon and username

                Text(
                    style = TextStyle(fontWeight = FontWeight.Medium,fontSize=22.sp),
                    text = "Hello, Conversa "+message,
                    color = Color.White // Set text color
                )
            }



            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                globalData.value.categories.forEach { it->
                    RoundedIconButtonRow(color=Color(0xFFFFA500),
                        icon = Icons.Default.MeetingRoom,text=it.name,
                        onClick = {
                            val gson: Gson = GsonBuilder().create()
                            val category = gson.toJson(it)

                            navController.navigate(
                                "${StringConstants.CATEGORYVIEWER}/{category}" //Just modify your route accordingly
                                    .replace(
                                        oldValue = "{category}",
                                        newValue = category.replace("#ESP","ESP")
                                    )
                            )
                        })
                }

                RoundedIconButtonRow(color=Color.Black,
                    icon = Icons.Default.Add,text="Add Room",
                    onClick = {

                   navController.navigate(StringConstants.AddRoom)
                    })

            }
            Box(     modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp))
                .shadow(2.dp, spotColor = Color.Red) // Apply shadow here
                .background(Color(0xFF095F59))
                .padding(8.dp))
            {
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp) ,
                    style = TextStyle(fontWeight = FontWeight.Medium,fontSize=18.sp),
                    text = "Scence",
                    color = Color.White // Set text color
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RoundedIconButton(color=Color.Red,icon = Icons.Default.WbSunny,text="Good Morning", onClick = {

                })
                RoundedIconButton(color=Color.Black, icon = Icons.Default.ModeNight,text="Good Night", onClick = {

                })
                RoundedIconButton(color=Color(0xFFFF7F50),icon = Icons.Default.RunCircle,text="Away From Home", onClick = {

                })
                RoundedIconButton(color=Color.Red,icon = Icons.Default.Festival,text="Festival Scene", onClick = {

                })
                RoundedIconButton(color=Color(0xFF3E2723),icon = Icons.Default.PlayForWork,text="Relaxation Scene", onClick = {

                })
                RoundedIconButton(color=Color(0xFF800080),icon = Icons.Default.Movie,text="Movie Time", onClick = {

                })
                RoundedIconButton(color=Color.Blue,icon = Icons.Default.Security,text="Secure Mode", onClick = {

                })
                RoundedIconButton(color=Color.Magenta,icon = Icons.Default.Emergency,text="Emergency Scene", onClick = {

                })
            }

            Box(     modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(10.dp))
                .shadow(2.dp, spotColor = Color.Red) // Apply shadow here
                .background(Color(0xFF095F59))
                .padding(8.dp))
            {
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp) ,
                    style = TextStyle(fontWeight = FontWeight.Medium,fontSize=18.sp),
                    text = "Running..",
                    color = Color.White // Set text color
                )
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RoundedCardButton(color=Color(0xFFFFA500),icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })

                RoundedCardButton(color=Color.Magenta,icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })

                RoundedCardButton(color=Color(0xFF9966CC),icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })
                RoundedCardButton(color=Color.Gray,icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })

                RoundedCardButton(color=Color(0xFFFFA500),icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })
                RoundedCardButton(color=Color(0xFF800080),icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })

                RoundedCardButton(color=Color(0xFFFFA500),icon = Icons.Default.Favorite,text="Favorites", onClick = {

                })



            }


        }
        FloatingActionButton(
            onClick = {
                navController.navigate(StringConstants.ADDDEVICE)
            },
            // Change background color based on the theme
            contentColor = if (isDarkMode) Color.Black else Color.White, // Change icon color
            modifier = Modifier .offset(x= (-10).dp,y=(-10).dp)
                .shadow(8.dp, RoundedCornerShape(32.dp),
                )
                .size(56.dp).background (if (isDarkMode) Color.White else Color.Black)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay, // Light and dark mode icons
                contentDescription = "Toggle Theme"
            )
        }
    }
}

@Composable
fun RoundedIconButton(icon: ImageVector, onClick: () -> Unit, text: String="",color:Color=Color.Black) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(10.dp),) // Apply shadow here
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalAlignment = Alignment.CenterVertically, // Center align vertically
            verticalArrangement = Arrangement.spacedBy(5.dp) // Space between icon and text
        ) {
            Box(
                modifier = Modifier.size(36.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)) // Shadow effect
                    .background(color,
                        shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.padding(6.dp),
                    imageVector = icon,
                    contentDescription = null, // Provide a description if needed
                    tint = Color.White // Set icon color to white for contrast
                )
            }
            Text(
                style = TextStyle(fontWeight = FontWeight.Medium,fontSize=18.sp),
                text = text,
                color = Color.Black // Set text color
            )

        }
    }
}

@Composable
fun RoundedIconButtonRow(icon: ImageVector, onClick: () -> Unit, text: String="",color:Color=Color.Black) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(36.dp),) // Apply shadow here
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically, // Center align vertically
            horizontalArrangement = Arrangement.spacedBy(5.dp) // Space between icon and text
        ) {
            Box(
                modifier = Modifier.size(36.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)) // Shadow effect
                    .background(color,
                        shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.padding(6.dp),
                    imageVector = icon,
                    contentDescription = null, // Provide a description if needed
                    tint = Color.White // Set icon color to white for contrast
                )
            }
            Text(
                style = TextStyle(fontWeight = FontWeight.Medium,fontSize=18.sp),
                text = text,
                color = Color.Black // Set text color
            )

        }
    }
}

@Composable
fun RoundedCardButton(icon: ImageVector, onClick: () -> Unit, text: String="",color:Color=Color.Black) {
    Box(
        modifier = Modifier.fillMaxHeight().width(150.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(10.dp),) // Apply shadow here
            .background(Color(0xFF838382))
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // Ensures Column fills the available width
                .fillMaxHeight() , // Padding inside the Column
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(1f), // Fills the full width of the Column
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .background(Color(0xFF095F59), shape = RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.padding(6.dp),
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                //   CustomSwitchWithIcon()
                VerticalSwitchButton1()
            }

            Text(
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
                text = text,
                color = Color.White
            )
        }
    }

}
@Composable
fun VerticalToggleButton( ) {
    var isOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .background(Color.Gray, shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        // Background for the toggle button
        val backgroundColor = if (isOn) Color.Green else Color.Red
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .clickable { isOn=!isOn } // Handle click to toggle state
        ) {
            // Content (icon or text) inside the toggle
            Text(
                text = if (isOn) "ON" else "OFF",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
//            Switch(checked = isOn, onCheckedChange = {isOn=it})

        }
    }
}



@Composable
fun VerticalSwitchButton1(imageVector: ImageVector=Icons.Default.PowerSettingsNew) {
    // State to determine if the switch is checked
    val isChecked = remember { mutableStateOf(false) }

    // Animate the thumb position based on isChecked state
    val thumbPosition by animateDpAsState(
        targetValue = if (!isChecked.value) 30.dp else 2.dp
    )

    Box(
        modifier = Modifier
            .width(34.dp) // Width for the switch
            .height(64.dp) // Height for the switch
            .background(if(isChecked.value) Color.White else  Color(0xFF3c3e3e), shape = RoundedCornerShape(34.dp)) // Background color
            .clickable { isChecked.value = !isChecked.value } ,

        ) {
        // Thumb that moves vertically
        Box(
            modifier = Modifier
                .size(30.dp)
                .offset(x=2.dp,y=thumbPosition,)
                .background(if(isChecked.value) Color.Magenta else Color(0xFF7f8182), shape = RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = "Lights Icon",
                tint =  Color.White,
                modifier = Modifier.size(20.dp) // Size of the icon inside the thumb
            )
        }
    }
}




@Composable
fun CustomSwitchWithIcon() {
    var isChecked by remember { mutableStateOf(false) }
    val thumbPosition by animateDpAsState(
        targetValue = if (isChecked) 30.dp else 0.dp
    )
    val switchBackgroundColor by animateColorAsState(
        targetValue = if (isChecked) Color.Green else Color.Gray
    )
    Box(
        modifier = Modifier
            .width(60.dp) // Width for the switch
            .height(30.dp) // Height for the switch
            .shadow(8.dp, shape = MaterialTheme.shapes.small) // Shadow effect
            .background(Color.LightGray, shape = MaterialTheme.shapes.small) // Background color
            .clickable { isChecked = !isChecked }, // Toggle the state on click
        contentAlignment = Alignment.Center // Center the content vertically and horizontally
    ) {
        // The thumb with the shutdown icon
        Box(
            modifier = Modifier
                .size(30.dp)
                .offset(y = thumbPosition,x=2.dp) // Move the thumb vertically
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Shutdown Icon",
                tint = if (isChecked) Color.Green else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomSwitchWithIcon() {
    CustomSwitchWithIcon()
}

@Composable
fun VerticalSwitchButton() {
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isChecked) "Switch is ON" else "Switch is OFF")

        Spacer(modifier = Modifier.height(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
    }
}




@Composable
fun SecondScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Second Screen")
    }
}