package com.example.intento2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.intento2.dataclass.AnsweredQuestionsTypeConverter
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.HighScores
import com.example.intento2.dataclass.Options
import com.example.intento2.dataclass.Progress
import com.example.intento2.dataclass.QuestionsTypeConverter
import com.example.intento2.dataclass.SavedQuestions
import com.example.intento2.dataclass.TypeConvertered
import com.example.intento2.dataclass.User
import com.example.intento2.dataclass.UserSelectionTypeConverter

@Database(
    entities = [User::class, GameSettings::class, SavedQuestions::class, Progress::class, HighScores::class, Options::class],
    version = 3, exportSchema = false
)
@TypeConverters(QuestionsTypeConverter::class, TypeConvertered::class, AnsweredQuestionsTypeConverter::class, UserSelectionTypeConverter::class)

abstract class MyAppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun GameSettingsDao(): GameSettingsDao
    abstract fun SavedQuestionsDao(): SavedQuestionsDao
    abstract fun progressDao(): ProgressDao
    abstract fun highScoresDao(): HighScoresDao
    abstract fun OptionsDao(): OptionsDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "my_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
