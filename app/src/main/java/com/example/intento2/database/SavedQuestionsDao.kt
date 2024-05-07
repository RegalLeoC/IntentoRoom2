package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.intento2.coded.Questions
import com.example.intento2.dataclass.SavedQuestions

@Dao
interface SavedQuestionsDao {
    @Insert
    suspend fun insertQuestion(question: SavedQuestions)

    @Query("SELECT id FROM Question WHERE questionText = :questionText")
    suspend fun getQuestionIdByQuestionText(questionText: String): Int?

    @Query("SELECT * FROM Question WHERE id = :questionId")
    fun getQuestionById(questionId: Int): SavedQuestions?

    @Query("SELECT id FROM Question WHERE gameId = :gameId")
    suspend fun getQuestionIdsByGameId(gameId: Int): List<Int>

    @Query("SELECT * FROM Question WHERE id = :questionId")
    suspend fun getQuestionContentById(questionId: Int): SavedQuestions?


    //@Query("SELECT content FROM Question WHERE id = :questionId")
    //suspend fun getQuestionContentById(questionId: Int): Questions?

}