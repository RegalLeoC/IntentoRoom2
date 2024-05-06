package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.intento2.dataclass.Options


@Dao
interface OptionsDao {

    @Query("SELECT id FROM Options WHERE id = :id")
    suspend fun getExistingUniqueId(id: Int): Int?

    @Insert
    suspend fun insertAnswerOption(options: Options)
}
