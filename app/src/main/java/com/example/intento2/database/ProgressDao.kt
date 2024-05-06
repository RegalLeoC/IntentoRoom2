package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.intento2.dataclass.Progress

@Dao
interface ProgressDao {
    @Insert
    suspend fun insertProgress(progress: Progress)

    @Query("SELECT * FROM Progress WHERE gameId = :gameId")
    suspend fun getProgressByGameId(gameId: Int): Progress?

    @Update
    suspend fun updateProgress(progress: Progress)

    @Query("SELECT * FROM Progress WHERE userId = :userId")
    suspend fun getProgressByUserId(userId: Int): Progress?

}