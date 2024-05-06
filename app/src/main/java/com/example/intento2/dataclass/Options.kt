package com.example.intento2.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Options(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "uniqueId")
    val uniqueId: Int?,
    val state: String = "Unanswered",
    val optionText: String,
    val correct: Boolean? = false,
    val questionId: Int?

)