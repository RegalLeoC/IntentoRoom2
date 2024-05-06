package com.example.intento2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.intento2.dataclass.User


@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT id FROM User WHERE active = 1 LIMIT 1")
    suspend fun getActiveUserId(): Int

    @Query("SELECT * FROM User WHERE id = :userId")
    suspend fun getUserById(userId: Int): User

    @Query("UPDATE User SET pendingGame = 0 WHERE id = :userId")
    suspend fun setPendingGameToFalse(userId: Int)

    @Query("UPDATE User SET pendingGame = 1 WHERE id = :userId")
    suspend fun setPendingGameToTrue(userId: Int)

}