package com.example.intento2.coded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JuegoReanudado : AppCompatActivity() {

    private lateinit var buttonContainer: LinearLayout
    private lateinit var questionTextView: TextView
    private lateinit var topicImageView: ImageView
    private lateinit var questionNumberTextView: TextView
    private lateinit var hintTextView: TextView
    private lateinit var hintButton: Button

    private var questionOptions: MutableMap<Int, List<String>> = mutableMapOf() // List of all options we generate initially per question
    private var enabledWrongOptions: MutableMap<Int, MutableList<String>> = mutableMapOf() // List of wrong options that are enabled
    private var disabledWrongOptions: MutableMap<Int, List<String>> = mutableMapOf() // List of wrong options that were disabled by using hint
    private var answeredQuestions: MutableMap<Int, Boolean> = mutableMapOf() // Questions answered through manual selection
    private var answeredQuestionsHint:  MutableMap<Int, Boolean> = mutableMapOf() // Questions answered through hint
    private var userSelection: MutableMap<Int, String?> = mutableMapOf() //Right answers selected by the user
    private var hintSelection: MutableMap<Int, String?> = mutableMapOf()

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

    private lateinit var progress: Progress
    private lateinit var gameSettings: GameSettings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_reanudado)

        db = MyAppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.Main) {
            activeUserId = getActiveUserId()
            gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)
            progress = db.progressDao().getProgressByUserId(activeUserId)!!

            questionIndex = progress.questionIndex!!
            settingsId = gameSettings.id
            cluesActive = gameSettings.clues
            difficult = gameSettings.difficulty
            numberOfQuestions = gameSettings.numQuestions

            if(gameSettings.clues == false){
                hintButton.isVisible = false
                hintButton.isEnabled = false
            }

            for (i in 0 until numberOfQuestions) {
                answeredQuestions[i] = progress.answeredQuestions[i] ?: false
                answeredQuestionsHint[i] = progress.answeredQuestionsHint[i] ?: false
                userSelection[i] = progress.userSelection[i]
                hintSelection[i] = progress.hintSelection[i]


            }

            withContext(Dispatchers.IO) {
                db.progressDao().updateProgress(progress)
            }

            // Initialize layout after database operations
            buttonContainer = findViewById(R.id.buttonContainer)
            questionTextView = findViewById(R.id.questionTextView)
            topicImageView = findViewById(R.id.topicImageView)
            questionNumberTextView = findViewById(R.id.questionNumberTextView)
            hintTextView = findViewById(R.id.hintTextView)
            hintButton = findViewById(R.id.hintButton)


            setUpClickListeners()

            // Select random questions and update UI
            selectRandomQuestions()


        }


    }

    private fun selectRandomQuestions() {
        TODO("Not yet implemented")
    }

    private fun setUpClickListeners() {
        //Exploration
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            nextQuestion()
        }

        findViewById<Button>(R.id.prevButton).setOnClickListener {
            previousQuestion()
        }

        //Use hint
        if(gameSettings.clues){
            hintButton.setOnClickListener {
                //useHint()
            }
        }
    }

    private fun previousQuestion() {
        questionIndex = (questionIndex - 1 + questions.size) % numberOfQuestions
        updateQuestion()
    }

    private fun nextQuestion() {
        questionIndex = (questionIndex + 1) % numberOfQuestions
        updateQuestion()
    }

    private suspend fun getActiveUserId(): Int {
        var userId = db.userDao().getActiveUserId()
        return userId;
    }
}