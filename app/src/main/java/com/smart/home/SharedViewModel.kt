package com.smart.home

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

class GlobalSharedViewModel : ViewModel() {

    // LiveData for the global view model data
    private val _globalViewModelData = MutableLiveData<GlobalViewModel>()
    val globalViewModelData: LiveData<GlobalViewModel> = _globalViewModelData

    init {
        // Initialize with default values
        _globalViewModelData.value = GlobalViewModel()
    }

    // Function to update the GlobalViewModel and notify observers
    fun updateGlobalViewModelData(newData: GlobalViewModel) {
        _globalViewModelData.value = newData
    }

    // Function to update specific device, sensor, scene, or category
//    fun updateDevice(categoryId: String, deviceId: String, updatedDevice: Device) {
//        val currentData = _globalViewModelData.value?.copy() ?: return
//        currentData.categories.find { it.id == categoryId }?.devices?.let { devices ->
//            devices.replaceAll { if (it.id == deviceId) updatedDevice else it }
//        }
//        _globalViewModelData.value = currentData
//    }

    // Add more functions for updating sensor metadata, scenes, etc. as needed
}


class SharedViewModel : ViewModel() {
    // StateFlow for managing the message state
    private val _message = MutableStateFlow("Initial Message")
    val message: StateFlow<String> get() = _message
    // Function to update the message
    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }

    private val _globalviewmodal = MutableStateFlow(GlobalSharedViewModel())
    val globalviewmodal: StateFlow<GlobalSharedViewModel> get() = _globalviewmodal
    fun updateMessage1(globalviewmodal: GlobalSharedViewModel) {
        _globalviewmodal.value = globalviewmodal
    }
}

@Serializable
data class GlobalViewModel(
    @SerializedName("sensors")
    val sensors: List<String> = listOf("ldr", "pir", "temperature"),
    @SerializedName("devicePairTypes")
    val devicePairTypes : List<String> =listOf("Fan", "TV", "Light Switch", "Night Lamp"),
    @SerializedName("device")
    val device : MutableList<DeviceType> = mutableListOf(),
    @SerializedName("sensorMetaData")
    val sensorMetaData: SensorMetaData = SensorMetaData(),
    @SerializedName("devicesTypes")
    val devicesTypes: List<PairDevice> = listOf(),
    @SerializedName("scenes")
    val scenes: List<Scene> = listOf(
        Scene(
        id = "1",
        name = "Good Morning",
        categories = listOf(
            Category(
                id = "LR",
                name = "Living Room",
                devices = listOf(
                    Device(id = "LR_FAN", type = "Fan", name = "Living Room Fan")
                )
            )
        ),
        schedules = listOf(
            ScheduleMetaData(
                priority = 1,
                enabled = true,
                startTime = "07:00",
                endTime = "09:00"
            )
        )
    )),
    @SerializedName("categories")
    var categories: MutableList<Category> = mutableListOf(
        Category(id = "LR", name = "Living Room", devices = listOf(
            Device(id = "LR FAN", type = "FAN", name = "Living Room Fan"),
            Device(id = "LR TV", type = "TV", name = "Living Room TV")
        )),
        Category(id = "BR", name = "Bedroom", devices = emptyList())
    )

)
@Serializable
data class SensorMetaData(
    @SerializedName("priority")
    val priority: Int = 0,
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("value")
    val value: Int = 1,
    @SerializedName("triggerAt")
    val triggerAt: Int = 1,
    @SerializedName("type")
    val type: String = "digital/analog",
    @SerializedName("relayon")
    val relayon: List<String> = listOf(),
    @SerializedName("schedule")
    val schedule: List<ScheduleMetaData> = listOf()
)
@Serializable
data class ScheduleMetaData(
    @SerializedName("priority")
    val priority: Int = 0,
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("startTime")
    val startTime: String = "22:00",
    @SerializedName("endTime")
    val endTime: String = "06:00",
    @SerializedName("deviceMode")
    val deviceMode: Boolean = true,
    @SerializedName("metaData")
    val metaData: MetaData = MetaData(),
    @SerializedName("sensors")
    val sensors: List<SensorMetaData> = listOf()
)
@Serializable
data class MetaData(
    @SerializedName("range")
    val range: Range = Range(),
    @SerializedName("isRGB")
    val isRGB: Boolean = false,
    @SerializedName("rgbColor")
    val rgbColor: String = "#f00",
)
@Serializable
data class DeviceType(
    @SerializedName("deviceName")
    val deviceName: String ="",
    @SerializedName("deviceMetaData")
    val deviceMetaData :MetaData = MetaData(),
)
@Serializable
data class PairDevice(
    @SerializedName("id")
    val id: String="",
    @SerializedName("name")
    val name: String="",
    @SerializedName("icon")
    var icon: String="",
    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("globalStatus")
    val globalStatus: Boolean = false,
    @SerializedName("deviceType")
    var deviceType: DeviceType = DeviceType(),   //fan , light
    @SerializedName("schedule")
    val schedule: List<ScheduleMetaData> = listOf(),
    @SerializedName("initData")
    val initData: MetaData = MetaData(),
)
@Serializable
data class Range(
    @SerializedName("min")
    val min:  Int = 1,
    @SerializedName("max")
    val max:  Int = 100,
    @SerializedName("stepSize")
    val stepSize: Int = 1,
    @SerializedName("value")
    val value:  Int = 1
)
@Serializable
data class Category(
    @SerializedName("id")
    val id: String="",
    @SerializedName("name")
    val name: String="",
    @SerializedName("devic  es")
    val devices: List<Device> = listOf()
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


