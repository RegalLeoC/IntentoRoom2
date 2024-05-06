package com.example.intento2.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Game_settings")
data class GameSettings(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "userId")
    val userId: Int?, // Foreign key to User
    var difficulty: String = "Normal",
    var numQuestions: Int = 10,
    var topic1: Boolean = true,
    var topic2: Boolean = true,
    var topic3: Boolean = true,
    var topic4: Boolean = true,
    var topic5: Boolean = true,
    var topic6: Boolean = true,
    var clues: Boolean = true
) {
    companion object {
        fun defaultInstance(userId: Int?): GameSettings {
            return GameSettings(
                id = 0, // Set the default ID here
                userId = userId,
                difficulty = "Normal",
                numQuestions = 10,
                topic1 = true,
                topic2 = true,
                topic3 = true,
                topic4 = true,
                topic5 = true,
                topic6 = true,
                clues = true
            )
        }
    }
}