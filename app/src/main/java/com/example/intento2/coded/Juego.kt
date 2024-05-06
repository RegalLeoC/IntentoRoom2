package com.example.intento2.coded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NullPointerException

class Juego : AppCompatActivity() {

    private lateinit var buttonContainer: LinearLayout
    private lateinit var questionTextView: TextView
    private lateinit var topicImageView: ImageView
    private lateinit var questionNumberTextView: TextView
    private lateinit var hintTextView: TextView
    private lateinit var hintButton: Button

    // Maps (dictionaries) to follow
    private var questionOptions: MutableMap<Int, List<String>> = mutableMapOf() // List of all options we generate initially per question
    private var enabledWrongOptions: MutableMap<Int, MutableList<String>> = mutableMapOf() // List of wrong options that are enabled
    private var disabledWrongOptions: MutableMap<Int, List<String>> = mutableMapOf() // List of wrong options that were disabled by using hint
    private var answeredQuestions: MutableMap<Int, Boolean> = mutableMapOf() // Questions answered through manual selection
    private var answeredQuestionsHint:  MutableMap<Int, Boolean> = mutableMapOf() // Questions answered through hint
    private var userSelection: MutableMap<Int, String?> = mutableMapOf() //Right answers selected by the user
    private var hintSelection: MutableMap<Int, String?> = mutableMapOf() //Right answers selected by the hint


    private var questionIndex: Int = 0;
    private lateinit var topics: Array<Topics>
    private lateinit var questions: List<Questions>

    private var hintsAvailable: Int = 3;
    private var hintStreak: Int = 0;

    private var hintsUsed: Int = 0;
    private var finalScore: Int = 0;
    private var correctAnswers: Int = 0;
    private var difficultyMultiplier: Double = 1.0


    private lateinit var db: MyAppDatabase
    private var numberOfQuestions: Int = 0;
    private var gameId: Int = 0;
    private var activeUserId: Int = 0;
    private var settingsId: Int? = 0;
    private var uniqueId: Int? = 0;
    private var difficult: String? = null;
    private var cluesActive: Boolean? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        db = MyAppDatabase.getDatabase(applicationContext)

        GlobalScope.launch(Dispatchers.Main) {
            uniqueId = generateUniqueId()
            gameId = generateRandomGameId()
            activeUserId = getActiveUserId()
            val gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)

            settingsId = gameSettings.id
            cluesActive = gameSettings.clues
            difficult = gameSettings.difficulty
            numberOfQuestions = gameSettings.numQuestions

            val progress = Progress(
                gameId,
                settingsId,
                activeUserId,
                score = 0,
                cluesActive!!,
                uniqueId
            )

            // Insert progress and update pending game status asynchronously
            withContext(Dispatchers.IO) {
                db.progressDao().insertProgress(progress)
                db.userDao().setPendingGameToTrue(activeUserId)
            }

            topics = Topics.values()

            topics = Topics.updateTopics(
                gameSettings.topic1,
                gameSettings.topic2,
                gameSettings.topic3,
                gameSettings.topic4,
                gameSettings.topic5
            ).toTypedArray()


            for (i in 0 until numberOfQuestions) {
                answeredQuestions[i] = false
                progress.answeredQuestions[i] = answeredQuestions[i] ?: false

                answeredQuestionsHint[i] = false
                progress.answeredQuestionsHint[i] = answeredQuestionsHint[i] ?: false

                userSelection[i] = null
                progress.userSelection[i] = userSelection[i]

                hintSelection[i] = null
                progress.hintSelection[i] = hintSelection[i]

            }

            withContext(Dispatchers.IO) {
                db.progressDao().updateProgress(progress)
            }

            // Layout initialization
            buttonContainer = findViewById(R.id.buttonContainer)
            questionTextView = findViewById(R.id.questionTextView)
            topicImageView = findViewById(R.id.topicImageView)
            questionNumberTextView = findViewById(R.id.questionNumberTextView)
            hintTextView = findViewById(R.id.hintTextView)
            hintButton = findViewById(R.id.hintButton)
        }
    }


    private suspend fun generateUniqueId(): Int {

        var uniqueIds: Int = 0
        do {
            uniqueIds = (1..1000).random()
        }while (uniqueId?.let { db.OptionsDao().getExistingUniqueId(it) } != null)

        return uniqueIds
    }

    private suspend fun getActiveUserId(): Int {
        var userId = db.userDao().getActiveUserId()
        return userId;
    }

    private suspend fun generateRandomGameId(): Int {
        var randomId: Int = 0
        do {
            randomId = (1..1000).random() // Generate a random game ID
        } while (db.progressDao().getProgressByGameId(randomId) != null)// Check if ID already exists in the table
        return randomId
    }


}