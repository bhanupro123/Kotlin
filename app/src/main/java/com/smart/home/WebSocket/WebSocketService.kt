import android.content.Context
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.smart.home.Category
import com.smart.home.DeviceTypeModel
import com.smart.home.SharedViewModel
import com.smart.home.SnackbarManager
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule
import java.util.Timer

class WebSocketService(private val context: Context) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)  // Disable timeout, WebSocket is long-lived
        .build()

    private lateinit var webSocket: WebSocket
    private var reconnectionAttempts = 0
    private val maxReconnectionAttempts = 100
    private val baseReconnectionDelay = 2000L // Start with 2 seconds

    fun connectWebSocket(sharedViewModel: SharedViewModel) {
        val sharedData= sharedViewModel.globalViewModelData.value
        val request = Request.Builder()
            .url("ws://192.168.1.16:8080") // Your WebSocket URL
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectionAttempts = 0 // Reset attempts on successful connection
                SnackbarManager.showSnackbar("WebSocket Connected!")
                webSocket.send("AllDeviceTypes")
                webSocket.send("Rooms")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Receiving: $text")

                 try {
                     if(text.startsWith("AllDeviceTypes"))
                     {
                     val devicesList: Array<DeviceTypeModel> = GsonBuilder().create().fromJson(
                         text.replace("AllDeviceTypes", ""),
                         Array<DeviceTypeModel>::class.java
                     )
                     val mutableDevicesList: MutableList<DeviceTypeModel> =
                         devicesList.toMutableList()
                     sharedData?.devicesTypesModels = mutableDevicesList

                 }
                     else if(text.startsWith("Rooms"))
                     {
                         val devicesList: Array<Category> = GsonBuilder().create().fromJson(
                             text.replace("Rooms", ""),
                             Array<Category>::class.java
                         )
                         val mutableDevicesList: MutableList<Category> =
                             devicesList.toMutableList()
                         sharedData?.categories = mutableDevicesList

                     }
                     else if(text.startsWith("AllDeviceTypes"))
                     {

                     }
                     else   {
                         SnackbarManager.showSnackbar(text)
                         sharedViewModel.updateMessage(text)
                     }
                 }
                 catch (e:Error)
                 {
                  println(e)
                 }


            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Receiving bytes: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Closing: $code / $reason")
                webSocket.close(1000, "Connection closed by client.")
                attemptReconnection(sharedViewModel)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Failure: ${t.message}")
                SnackbarManager.showSnackbar("Error: ${t.message}")
                attemptReconnection(sharedViewModel)
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        if (::webSocket.isInitialized) {
            SnackbarManager.showSnackbar("Sending.....")
            webSocket.send(message) // Send the message
        } else {
            println("WebSocket is not initialized!")
        }
    }

    fun closeWebSocket() {
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Goodbye!")
        }
    }

    private fun attemptReconnection(sharedViewModel: SharedViewModel) {
        if (reconnectionAttempts < maxReconnectionAttempts) {
            reconnectionAttempts++
            val reconnectionDelay = baseReconnectionDelay * reconnectionAttempts
            println("Reconnecting in ${reconnectionDelay / 1000} seconds... (Attempt $reconnectionAttempts)")

            Timer().schedule(reconnectionDelay) {
                SnackbarManager.showSnackbar("Reconnecting....")
                connectWebSocket(sharedViewModel)
            }
        } else {
            println("Max reconnection attempts reached. Giving up.")
            SnackbarManager.showSnackbar("Failed to reconnect after $maxReconnectionAttempts attempts.")
        }
    }
}
