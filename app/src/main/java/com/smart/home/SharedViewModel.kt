package com.smart.home


import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable




class SharedViewModel : ViewModel() {
    // StateFlow for managing the message state

    private val _message = MutableStateFlow("Initial Message")
    val message: StateFlow<String> get() = _message

    // Function to update the message
    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }
    private val _globalViewModelData = MutableStateFlow(GlobalViewModel())
    val globalViewModelData: StateFlow<GlobalViewModel> get() = _globalViewModelData

    // Function to update GlobalViewModel
    fun updateGlobalData(newData: GlobalViewModel) {
        _globalViewModelData.value = newData
    }

}

@Serializable
data class DeviceTypeModel(
    @SerializedName("brightness")
    val brightness: Int?=0,
    @SerializedName("maxBrightness")
    val maxBrightness: Int?=100  ,
    @SerializedName("icon")
    val icon: String?="",
    @SerializedName("id")
    val id: Int?=0,
    @SerializedName("isRGB")
    val isRGB: Int?=0,
    @SerializedName("maxLevel")
    val maxLevel: Int?=100,
    @SerializedName("level")
    val level: Int?=1,
    @SerializedName("mode")
    val mode: String?="0",
    @SerializedName("modeValue")
    val modeValue: String?="",
    @SerializedName("more")
    val more: String?="",
    @SerializedName("name")
    var name: String?="",
    @SerializedName("rgbColor")
    val rgbColor: String?="",
)

@Serializable
data class GlobalViewModel(
    @SerializedName("sensors")
    val sensors: List<String> = listOf("ldr", "pir", "temperature"),
    @SerializedName("sensorMetaData")
    val sensorMetaData: SensorMetaData = SensorMetaData(),
    @SerializedName("devicesTypes")
    val devicesTypes: List<PairDevice> = listOf(),
    @SerializedName("devicesCat")
    val devicesCat: List<String> = listOf("FAN","TV","LIGHT","RGB"),
    @SerializedName("devicesTypesModels")
    var devicesTypesModels: MutableList<DeviceTypeModel> = mutableListOf(),
    @SerializedName("scenes")
    val scenes: List<Scene> = listOf(
        Scene(
        id = "1",
        name = "Good Morning",
        categories = listOf(

        ),
        schedules = listOf(
            ScheduleMetaData(
                priority = 1,
                enabled = 1,
                startTime = "07:00",
                endTime = "09:00"
            )
        )
    )),
    @SerializedName("categories")
    var categories: MutableList<Category> = mutableListOf(
     //   Category(id = "BR", name = "Bedroom", devices = emptyList())
    )

)
@Serializable
data class SensorMetaData(
    @SerializedName("id")
    val id: String="",
    @SerializedName("priority")
    val priority: Int = 0,
    @SerializedName("enabled")
    val enabled: Int = 0,
    @SerializedName("value")
    val value: Int = 1,
    @SerializedName("sensorName")
    val sensorName: String = "",
    @SerializedName("triggerAt")
    val triggerAt: Int = 1,
    @SerializedName("type")
    val type: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("relayon")
    val relayon: String = "",
)
@Serializable
data class ScheduleMetaData(
    @SerializedName("id")
    val id: Int=-1,
    @SerializedName("tableName")
    val tableName: String="",
    @SerializedName("name")
    val name: String="",
    @SerializedName("priority")
    val priority: Int = 0,
    @SerializedName("enabled")
    val enabled: Int = 0,
    @SerializedName("startTime")
    val startTime: String = "22:00",
    @SerializedName("endTime")
    val endTime: String = "06:00",
    @SerializedName("deviceMetaInfo")
    var deviceMetaInfo: DeviceMetaInfo = DeviceMetaInfo(),
    @SerializedName("sensors")
    val sensors: List<SensorMetaData> = listOf()
)




@Serializable
data class DeviceMetaInfo(
    @SerializedName("id")
    val id: String="",
    @SerializedName("name")
    var name: String="",
    @SerializedName("icon")
    var icon: String="",
    @SerializedName("enabled")
    val enabled: Int = 0,
    @SerializedName("_actionType")
    val _actionType: String = "", //timer , user , sensor
    @SerializedName("_running")
    val _running: Int = 0,
    @SerializedName("type")
    var type: String="FAN",
    @SerializedName("range")
    var range: Int = 1,
    @SerializedName("mode")
    val mode: String = "", //timer , user , sensor
    @SerializedName("destination")
    val destination: String = "#ESP1",
    @SerializedName("rgbColor")
    val rgbColor: String = "",
    @SerializedName("outputMode")
    val outputMode: String = "",  //animation
    @SerializedName("schedule")
    val schedule: List<ScheduleMetaData> = listOf(),
    @SerializedName("roomId")
    val roomId: String = "",
)


@Serializable
data class PairDevice(
    @SerializedName("name")
    val name: String="",
    @SerializedName("icon")
    var icon: String="",
    @SerializedName("enabled")
    val enabled: Int = 0,
    @SerializedName("actionType")
    val actionType: String = "", //timer , user , sensor
    @SerializedName("globalStatus")
    val globalStatus: Boolean = false,
    @SerializedName("deviceTypeModel")
    var deviceTypeModel: DeviceTypeModel = DeviceTypeModel(),   //fan , light
    @SerializedName("schedule")
    val schedule: List<ScheduleMetaData> = listOf(),
)

@Serializable
data class Category(
    @SerializedName("id")
    val id: String="",
    @SerializedName("name")
    val name: String="",
    @SerializedName("icon")
    val icon: String="",
    @SerializedName("description")
    val description: String="",
    @SerializedName("devices")
    val devices:MutableList<DeviceMetaInfo> = mutableListOf()
)
@Serializable
data class Device(
    @SerializedName("id")
    val id:  String="",
    @SerializedName("type")
    val type:  String="",
    @SerializedName("Name")
    val name: String="",
    @SerializedName("icon")
    val icon:  String=""
)
@Serializable
data class Scene(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("categories")
    val categories: List<Category> = listOf(),
    @SerializedName("schedules")
    val schedules: List<ScheduleMetaData> = listOf()
)


