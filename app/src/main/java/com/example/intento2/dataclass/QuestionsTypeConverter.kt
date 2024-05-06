package com.example.intento2.dataclass

import androidx.room.TypeConverter
import com.example.intento2.coded.Questions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuestionsTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun questionsToString(questions: Questions): String {
        return gson.toJson(questions)
    }

    @TypeConverter
    fun stringToQuestions(value: String): Questions {
        val type = object : TypeToken<Questions>() {}.type
        return gson.fromJson(value, type)
    }
}
