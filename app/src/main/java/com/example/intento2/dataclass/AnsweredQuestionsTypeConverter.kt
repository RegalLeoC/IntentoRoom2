package com.example.intento2.dataclass

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AnsweredQuestionsTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Map<Int, Boolean> {
        if (value == null) {
            return emptyMap()
        }
        val mapType = object : TypeToken<Map<Int, Boolean>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<Int, Boolean>): String {
        return gson.toJson(map)
    }
}