package com.example.intento2.dataclass

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserSelectionTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Map<Int, String?> {
        if (value == null) {
            return emptyMap()
        }
        val mapType = object : TypeToken<Map<Int, String?>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<Int, String?>): String {
        return gson.toJson(map)
    }
}