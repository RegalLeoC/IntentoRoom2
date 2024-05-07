package com.example.intento2.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.intento2.coded.Questions

@Entity(tableName="Question")
data class SavedQuestions(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "gameId")
    val gameId: Int, // Foreign key to Game_settings
    val state: String = "Unanswered",
    val difficulty: String = "Normal",
    val uniqueId: Int? = 0,
    val content: Questions?,
    val questionText: String? = null,
    val indexNum: Int? = 0
    //Topic
)