package com.notifii.lockers.Utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class SharedPref {
    fun setData(context: Context,key:String, data: String) {
        val sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(key, data)
            apply()
        }
    }
    fun getData(context: Context,key:String): String? {
        val sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }
    fun deleteData(context: Context, key: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply() // Or editor.commit() if you need synchronous saving
    }

}