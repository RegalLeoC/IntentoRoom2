package com.example.intento2.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val name: String,
    var pendingGame: Boolean? = false,
    val highestScore: Int? = null,
    val active: Boolean = false
)