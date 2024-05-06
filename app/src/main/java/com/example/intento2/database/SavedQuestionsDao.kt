package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.intento2.dataclass.SavedQuestions

@Dao
interface SavedQuestionsDao {
    @Insert
    suspend fun insertQuestion(question: SavedQuestions)

    @Query("SELECT id FROM Question WHERE questionText = :questionText")
    suspend fun getQuestionIdByQuestionText(questionText: String): Int?

}