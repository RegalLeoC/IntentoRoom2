package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.User


@Dao
interface GameSettingsDao {

    @Insert
    suspend fun insertGameSettings(gameSettings: GameSettings)

    @Query("SELECT * FROM Game_settings WHERE userId = :userId")
    suspend fun getGameSettingsByUserId(userId: Int): GameSettings

    @Update
    suspend fun updateGameSettings(gameSettings: GameSettings)

    @Query("SELECT * FROM Game_settings WHERE id = :settingsId LIMIT 1")
    suspend fun getGameSettingsById(settingsId: Int): GameSettings?

}