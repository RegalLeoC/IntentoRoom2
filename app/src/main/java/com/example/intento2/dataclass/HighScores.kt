package com.example.intento2.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName="HighScores", foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)])
data class HighScores(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "userId")
    val userId: Int, // Foreign key to User
    val name: String,
    val score: Double = 0.0,
    val clues: Boolean?
)