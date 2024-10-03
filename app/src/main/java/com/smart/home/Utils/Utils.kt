package com.notifii.lockers.Utils
import android.content.Context
import android.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import androidx.compose.ui.graphics.Color as color


fun hexToColor(value: String? = "#428dcc"): Int {
    try {
        if (value != null) {
            if(value.length==0)
                return Color.parseColor("#ff0000")
        }
        return Color.parseColor(value) // Code that may throw an exception
    } catch (e: IOException) {

    }
    return 0xff0000
}

fun getRandomString(length: Int=10): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
fun getRandomColor(): color {
    val red = Random.nextInt(256) // Generate a random value between 0 and 255 for Red
    val green = Random.nextInt(256) // Generate a random value between 0 and 255 for Green
    val blue = Random.nextInt(256) // Generate a random value between 0 and 255 for Blue
    return color(red, green, blue) // Create a Color from the RGB values
}

