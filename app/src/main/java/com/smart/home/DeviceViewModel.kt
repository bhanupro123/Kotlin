package com.smart.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
//import com.smart.home.DeviceMetaData.FanControlData
//
//class FanContolViewModel : ViewModel() {
//
//    private val _fanControlData = MutableLiveData<FanControlData>()
//    val fanControlData: LiveData<FanControlData> get() = _fanControlData
//
//    // Initialize the ViewModel with default values
//    init {
//        _fanControlData.value = FanControlData()
//    }
//
//    // Method to update the FanControlData
//    fun updateFanControlData(newData: FanControlData) {
//        _fanControlData.value = newData
//    }
//
//    // Method to serialize the FanControlData to JSON
//    fun toJson(): String {
//        val gson = Gson()
//        return gson.toJson(_fanControlData.value)
//    }
//
//    // Method to deserialize JSON to FanControlData
//    fun fromJson(json: String) {
//        val gson = Gson()
//        _fanControlData.value = gson.fromJson(json, FanControlData::class.java)
//    }
//}
