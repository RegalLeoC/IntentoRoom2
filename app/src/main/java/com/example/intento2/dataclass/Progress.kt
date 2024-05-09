package com.example.intento2.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Progress(
    @PrimaryKey val gameId: Int? = null,
    val settingsId: Int? = null,
    val userId: Int,
    val score: Int = 0,
    val clues: Boolean,
    val uniqueId: Int? = 0,
    var hintsUsed: Int? = 0,
    var correctAnswers: Int? = 0,
    var finalScore: Int? = 0,
    var questionOptions: MutableMap<Int, List<String>> = mutableMapOf(),
    var enabledWrongOptions: MutableMap<Int, MutableList<String>> = mutableMapOf(),
    var disabledWrongOptions: MutableMap<Int, List<String>> = mutableMapOf(),
    var answeredQuestions: MutableMap<Int, Boolean> = mutableMapOf(),
    var answeredQuestionsHint:  MutableMap<Int, Boolean> = mutableMapOf(),
    var userSelection: MutableMap<Int, String?> = mutableMapOf(),
    var hintSelection: MutableMap<Int, String?> = mutableMapOf(),
    var questionIndex: Int? = 0,
    var hintsAvailable: Int? = 3,
    var hintStreak: Int? = 0,

)
