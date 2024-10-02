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
import com.notifii.lockers.Network.APIS.Login.RecipientItem
import com.notifii.lockers.R
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import androidx.compose.ui.graphics.Color as color

val customFontFamily = FontFamily(
    Font(R.font.avenirheavy),  // Regular font
    Font(R.font.avenirlight),  // R
    Font(R.font.avenirmedium),  // R
    Font(R.font.avenirnextltproregular,FontWeight.Normal,FontStyle.Normal),
    Font(R.font.avenirnextltprobold,FontWeight.Bold,FontStyle.Normal),
    Font(R.font.avenirnextltprodemi,FontWeight.SemiBold,FontStyle.Normal),
    Font(R.font.avenirnextltproitalic,FontWeight.Normal,FontStyle.Italic),
)

fun defaultTextStyle(fontSize: Int = 20,colored: color=color.Black): TextStyle {
    return TextStyle(
        color=colored,
        fontFamily = customFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize.sp
    )
}

fun boldTextStyle(fontSize: Int = 22): TextStyle {
    return TextStyle(
        fontFamily = customFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp
    )
}
fun semiBoldTextStyle(fontSize: Int = 24): TextStyle {
    return TextStyle(
        fontFamily = customFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize.sp
    )
}
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

fun handleResponse(navController: NavController)
{

}

fun handleSessionExpired(navController: NavController,context: Context)
{
  SharedPref().deleteData(context,StringConstants.SHARED_MASTER_DATA)
    navController.navigate(StringConstants.LOGIN) {
        popUpTo(0) { inclusive = true }
    }
}
object StringConstants {
    const val PICK_UP_MY_PACKAGE = "PICK UP MY PACKAGE"
    const val PIN_VALIDATION="PIN_VALIDATION"
    const val LOCKER_HEAT_MAP="LOCKER HEAT MAP"
    const val PARCEL_BASIC_INFO="ParcelBasicInfo"
    const val DELIVER_PACKAGES = "DELIVER PACKAGES"
    const val SHARED_MASTER_DATA = "masterUser"
    const val HOME="HOME"
    const val SPLASH="SPLASH"
    const val LOGIN="LOGIN"
}

private fun removeSpecialCharacters(string: String): String {
    var string = string
    val specialCharacters = "[^ A-Za-z0-9]"
    if (string.matches("(.*)$specialCharacters(.*)".toRegex())) string =
        string.replace(specialCharacters.toRegex(), " ")
            .replace("[\" \"]{1,5}".toRegex(), " ")
    return string.trim { it <= ' ' }
}
fun isValidField(field: String?): Boolean {
    return field != null && field.trim { it <= ' ' }.isNotEmpty()
}
fun isSearchSuccessful(recipientItem:RecipientItem,
    isPreferredNameEnabled: Boolean,
    searchQuery: String
): Boolean {
    var searchQuery = searchQuery
    searchQuery = searchQuery.trim { it <= ' ' }.lowercase(Locale.getDefault())
    if (!searchQuery.isEmpty()) searchQuery = removeSpecialCharacters(searchQuery) else return false

    //Search In First, Last Name
    var firstLastName = ""
    if (isValidField(recipientItem.first_name)) firstLastName += recipientItem.first_name
    if (isValidField(recipientItem.last_name)) firstLastName += " $recipientItem.last_name"
    firstLastName = removeSpecialCharacters(firstLastName).lowercase()
    if (isValidField(firstLastName) && firstLastName.startsWith(searchQuery) || firstLastName.matches(
            "(.*) $searchQuery(.*)".toRegex()
        )
    ) return true

    var preferredLastName = ""
    if (isPreferredNameEnabled && isValidField(recipientItem.preferred_first_name)) preferredLastName += recipientItem.preferred_first_name
    if (isValidField(recipientItem.last_name)) preferredLastName += " $recipientItem.last_name"
    preferredLastName = removeSpecialCharacters(preferredLastName).lowercase()
    if (isValidField(preferredLastName) && preferredLastName.startsWith(searchQuery) || preferredLastName.matches(
            "(.*) $searchQuery(.*)".toRegex()
        )
    ) return true


    //Search In Last, First Name
    var lastFirstName = ""
    if (isValidField(recipientItem.last_name)) lastFirstName += recipientItem.last_name
    if (isValidField(recipientItem.first_name)) lastFirstName += " $recipientItem.first_name"
    lastFirstName = removeSpecialCharacters(lastFirstName).lowercase()
    if (isValidField(lastFirstName) && lastFirstName.startsWith(searchQuery) || lastFirstName.matches(
            "(.*) $searchQuery(.*)".toRegex()
        )
    ) return true


    //Search In Last, Preferred Name
    var lastPreferredName = ""
    if (isValidField(recipientItem.last_name)) lastPreferredName += recipientItem.last_name
    if (isPreferredNameEnabled && isValidField(recipientItem.preferred_first_name)) lastPreferredName += " $recipientItem.preferred_first_name"
    lastPreferredName = removeSpecialCharacters(lastPreferredName).lowercase()
    if (isValidField(lastPreferredName) && lastPreferredName.startsWith(searchQuery) || lastPreferredName.matches(
            "(.*) $searchQuery(.*)".toRegex()
        )
    ) return true


    //Search In Address
    if (isValidField(recipientItem.address1)) {
        val address1: String = recipientItem.address1.let { removeSpecialCharacters(it).lowercase()
        }
        return address1.startsWith(searchQuery) || address1.matches("(.*) $searchQuery(.*)".toRegex())
    }
    return false
}

fun provideFormattedRecipientName(recipientItem:RecipientItem, isPreferredNameEnabled: Boolean=false): String {
    val firstName = if(recipientItem.first_name.isNotEmpty()) recipientItem.first_name.trim { it <= ' ' } else ""
    val lastName =  if(recipientItem.last_name.isNotEmpty()) recipientItem.last_name.trim { it <= ' ' } else ""
    var formattedRecipientName = ""
    formattedRecipientName =
        if (isPreferredNameEnabled && recipientItem.preferred_first_name.isNotEmpty())
        {
            if (firstName.isEmpty()) "${recipientItem.preferred_first_name} $lastName"
            else if (recipientItem.preferred_first_name.equals(firstName,ignoreCase = true))
                "$firstName $lastName"
            else "${recipientItem.preferred_first_name} $firstName $lastName"
        }
        else "$firstName $lastName"
    return formattedRecipientName.trim { it <= ' ' }
}

object HideAndShow {
    const val HIDE = "hide"
    const val OPTIONAL="optional"
    const val REQUIRED="Required"
}
object RequiresHandicapLockerTypes {
    const val YES = "yes"
    const val NO="no"

}
object CompartmentSizes {
    const val S = "Small"
    const val M = "Medium"
    const val L="Large"
    const val XL="X Large"
    const val FULL = "Full Size"
    const val NA = "not-applicable"
}
fun getFractionValue(value: String):Float{

    if(value==CompartmentSizes.S) return 1f
    if(value==CompartmentSizes.M) return 2f
    if(value==CompartmentSizes.L) return 4f
    if(value==CompartmentSizes.XL) return 8f
    if(value==CompartmentSizes.FULL) return 16f
    if(value==CompartmentSizes.NA) return 6f
    return 1f;
}

object CompartmentStatus {
    const val AVAILABLE = "available"
    const val INUSE="in-use"
    const val BROKEN="broken"
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

fun handleNetworkError(msg: String = ""): String {
    return when {
        msg.startsWith("Unable to resolve host") -> "No internet connection"
        msg.startsWith("timeout") -> "Request timed out"
        msg.contains("failed to connect") -> "Failed to connect to the server"
        else ->msg
    }
}