package com.example.intento2.dataclass

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConvertered {

    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): Map<Int, List<String>> {
        val mapType = object : TypeToken<Map<Int, List<String>>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<Int, List<String>>): String {
        return gson.toJson(map)
    }
}
