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

    @Query("SELECT * FROM User WHERE active = 1 LIMIT 1")
    suspend fun getActiveUser(): User?

    @Query("DELETE FROM Progress WHERE userId = :userId")
    suspend fun deleteProgressByUserId(userId: Int)

    @Query("UPDATE User SET active = CASE WHEN id = :userId THEN 1 ELSE 0 END")
    suspend fun setActiveUser(userId: Int)

    @Query("UPDATE User SET active = 0 WHERE id != :userId")
    suspend fun deactivateAllUsersExcept(userId: Int)
    @Query("DELETE FROM Question WHERE uniqueId IN (SELECT uniqueId FROM Game_settings WHERE userId = :userId)")
    suspend fun deleteQuestionsByUserId(userId: Int)

    @Query("DELETE FROM Options WHERE uniqueId IN (SELECT uniqueId FROM Question WHERE uniqueId IN (SELECT id FROM Game_settings WHERE userId = :userId))")
    suspend fun deleteAnswerOptionsByUserId(userId: Int)

    @Query("SELECT name FROM User WHERE id = :userId")
    suspend fun getUserNameById(userId: Int): String?

    @Query("UPDATE User SET pendingGame = 0 WHERE id = :userId")
    suspend fun disablePendingGameStatus(userId: Int)

}