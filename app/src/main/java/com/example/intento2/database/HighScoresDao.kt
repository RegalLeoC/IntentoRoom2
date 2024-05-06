package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.intento2.dataclass.HighScores

@Dao
interface HighScoresDao {
    @Query("SELECT * FROM HighScores")
    fun getAllHighScores(): List<HighScores>

    @Query("SELECT * FROM HighScores WHERE userId = :userId")
    fun getHighScoresForUser(userId: Int): List<HighScores>

    @Query("SELECT * FROM HighScores WHERE id = :highScoresId")
    fun getHighScoresById(highScoresId: Int): HighScores?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScores(highScores: HighScores)

    @Update
    fun updateHighScores(highScores: HighScores)

    @Query("SELECT * FROM HighScores ORDER BY score DESC, clues ASC LIMIT 5")
    suspend fun getTop5HighScores(): List<HighScores>

    @Query("SELECT * FROM HighScores ORDER BY score DESC, clues ASC LIMIT 20")
    suspend fun getTop20HighScores(): List<HighScores>

    // Add other methods for highScores-related database operations
}