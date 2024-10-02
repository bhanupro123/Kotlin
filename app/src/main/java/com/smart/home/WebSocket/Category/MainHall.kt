package com.smart.home.WebSocket.Category
import com.smart.home.Devices.FanControlUIMulti
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smart.home.SharedViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tv
import com.smart.home.Devices.TVControlUI
import com.smart.home.Utils.ColorConstants
import com.smart.home.Utils.DeviceType
import com.smart.home.Utils.randomColor

@Composable
fun MainHall(navController: NavController, sharedViewModel: SharedViewModel) {
    var isDarkMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var deviceType by remember { mutableStateOf(DeviceType.FAN) }
    Box(modifier = Modifier.fillMaxSize().padding(20.dp).background(Color.White),
        contentAlignment = Alignment.BottomEnd) {
        Row(modifier = Modifier.fillMaxSize())
        {
            Box(modifier = Modifier.weight(0.3f).fillMaxHeight())
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(4.dp)
                ) {
                    // First row: "TV" and "Fan"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        ColorfulVerticalSwitch(  modifier = Modifier
                                .weight(1f).clickable {
                                     deviceType=DeviceType.FAN
                            }
                                .clip(RoundedCornerShape(10.dp))
                                .aspectRatio(0.75f) // Apply rounded corners
                                ,
                                imageVector = Icons.Default.Tv,)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.75f) ,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f).fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray)
                                    ,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "TV1", style = TextStyle(color = Color.White, fontSize = 16.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f).fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "TV2", style = TextStyle(color = Color.White, fontSize = 16.sp))
                            }
                         }
                    }

                    // Second row: "Light 1" and "Light 2"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Light 1", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Light 2", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Light 1", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Light 2", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }
                    }
                    // Third row: "Light 3" (full-width horizontal rectangle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f).clickable {
                                    deviceType=DeviceType.TV
                                }
                                .aspectRatio(2f) // Full-width horizontal rectangle
                                .background(randomColor())
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "TV", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }
                    }

                    // Fourth row: "Light 4" and "Night Lamp"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .background(randomColor())
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Light 4", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square
                                .background(randomColor())
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Night Lamp", style = TextStyle(color = Color.White, fontSize = 16.sp))
                        }
                    }
                }
            }
          Box(modifier = Modifier.weight(1f).fillMaxHeight())
          {
                      if(deviceType==DeviceType.FAN)
                      {
                          FanControlUIMulti()
                      }
              if(deviceType==DeviceType.TV)
              {
                  TVControlUI()
              }
          }
        }



        FloatingActionButton(
            onClick = {  },
            // Change background color based on the theme
            contentColor = if (isDarkMode) Color.Black else Color.White, // Change icon color
            modifier = Modifier .offset(x= (-10).dp,y=(-10).dp)
                .shadow(8.dp, RoundedCornerShape(32.dp),
                )
                .size(56.dp).background (if (isDarkMode) Color.White else Color.Black)
        ) {
            Icon(
                imageVector = Icons.Default.Light,
                contentDescription = "Lights Icon",
                tint = Color.Yellow, // Customize the tint color as needed
                modifier = Modifier.size(48.dp) // Adjust icon size
            )
        }
    }
}


@Composable
fun ColorfulVerticalSwitch( modifier : Modifier, imageVector: ImageVector) {
    val isChecked = remember { mutableStateOf(true) }
    val thumbPosition by animateDpAsState(
        targetValue = if (!isChecked.value) 30.dp else 2.dp
    )
    Box(
        modifier = modifier
            .background(if (isChecked.value) ColorConstants.primary else ColorConstants.background), // Background color for the outer box
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(64.dp)
                    .background(
                        if (isChecked.value) Color.White else Color(0xFFB0BEC5), // Background for the switch
                        shape = RoundedCornerShape(34.dp)
                    )
                    .clickable { isChecked.value = !isChecked.value },
            ) {
                // Thumb that moves vertically
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .offset(x = 2.dp, y = thumbPosition)
                        .background(
                            if (isChecked.value) ColorConstants.secondary else ColorConstants.inActive, // Thumb color
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = "Lights Icon",
                        tint = Color.White, // Icon color inside the thumb
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = "TV",
                style = TextStyle(color = Color.White, fontSize = 16.sp) // Text color
            )
        }
    }
}




@Composable
fun VerticalSwitchButton(modifier: Modifier,
                         imageVector: ImageVector=Icons.Default.PowerSettingsNew) {
    // State to determine if the switch is checked
    val isChecked = remember { mutableStateOf(false) }

    // Animate the thumb position based on isChecked state
    val thumbPosition by animateDpAsState(
        targetValue = if (!isChecked.value) 30.dp else 2.dp
    )
Box(modifier = modifier.background(if(isChecked.value)Color.Gray else Color.DarkGray),contentAlignment = Alignment.Center){

    Column (horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ){

        Box(
            modifier = Modifier
                .width(34.dp) // Width for the switch
                .height(64.dp) // Height for the switch
                .background(if(isChecked.value) Color.White else  Color.Gray,
                    shape = RoundedCornerShape(34.dp)) // Background color
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
        Text(text = "TV", style = TextStyle(color = Color.White, fontSize = 16.sp))
    }

}
}
