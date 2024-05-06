package com.example.intento2.coded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.Options
import com.example.intento2.dataclass.Progress
import com.example.intento2.dataclass.SavedQuestions
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

        lifecycleScope.launch(Dispatchers.Main) {
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

            // Initialize layout after database operations
            buttonContainer = findViewById(R.id.buttonContainer)
            questionTextView = findViewById(R.id.questionTextView)
            topicImageView = findViewById(R.id.topicImageView)
            questionNumberTextView = findViewById(R.id.questionNumberTextView)
            hintTextView = findViewById(R.id.hintTextView)
            hintButton = findViewById(R.id.hintButton)

            // Select random questions and update UI
            selectRandomQuestions()

        }
    }


    private fun updateQuestion() {
        val currentQuestion = questions[questionIndex]
        val currentTopic = topics.find { it.questions.contains(currentQuestion) } ?: Topics.MATHEMATICS
        topicImageView.setImageResource(currentTopic.imageResourceId)
        questionTextView.text = currentQuestion.text

        val questionNumberText = "${questionIndex + 1}/${numberOfQuestions}"
        questionNumberTextView.text = questionNumberText

        // Si es la primera vez que se visita la pregunta, se generan opciones para escoger
        if (questionOptions[questionIndex] == null) {
            GlobalScope.launch(Dispatchers.IO) {
                val questionId = db.SavedQuestionsDao().getQuestionIdByQuestionText(currentQuestion.text)
                val options = questionId?.let { generateQuestionsOptions(currentQuestion, it) }

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    questionOptions[questionIndex] = options ?: mutableListOf()
                    //createButtons(options!!)
                }
            }
        } else {
            //createButtons(questionOptions[questionIndex]!!)
        }

        for (i in 1..10) {
            val buttonId = resources.getIdentifier("bar$i", "id", packageName)
            val button = findViewById<Button>(buttonId)
            button.tag = i // Set the tag to the question number for each button
        }

        for (i in 1..10) {
            val buttonId = resources.getIdentifier("bar$i", "id", packageName)
            val button = findViewById<Button>(buttonId)
            //button.setOnClickListener { navigateToQuestion(button.tag as Int) }
        }
    }


    private suspend fun generateQuestionsOptions(question: Questions, questionId: Int): MutableList<String> {
        val options = mutableListOf<String>()
        options.add(question.correctAnswer)

        var numWrongAnswers = 0;

        when (difficult) {
            "Easy" -> numWrongAnswers = 1
            "Normal" -> numWrongAnswers = 2
            "Hard" -> numWrongAnswers = 3
        }

        val wrongAnswers = question.wrongAnswers.shuffled().take(numWrongAnswers)
        options.addAll(wrongAnswers)
        options.shuffle()

        options.forEachIndexed { index, optionText ->
            val isCorrect = optionText == question.correctAnswer
            val optionInstance = Options(
                id = generateRandomOptionsId(),
                uniqueId = uniqueId,
                optionText = optionText,
                correct = isCorrect,
                questionId = questionId
            )

            db.OptionsDao().insertOptions(optionInstance)
        }

        return options
    }

    private fun generateRandomOptionsId(): Int? {
        var newId: Int? = null

        lifecycleScope.launch(Dispatchers.IO) {
            do {
                val randomId = (0 until 999).random()
                val existingOption = db.OptionsDao().getAnswerOptionById(randomId)
                if (existingOption == null) {
                    newId = randomId
                }
            } while (newId == null)
        }

        return newId
    }


    private fun selectRandomQuestions() {

        GlobalScope.launch(Dispatchers.IO) {

            val gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)

            val allQuestions = topics.flatMap { it.questions }.toMutableList()
            questions = mutableListOf()

            repeat(numberOfQuestions) {
                val randomQuestion = allQuestions.random()
                val randomId = (0..1000).random()


                val difficultylevel = gameSettings.difficulty
                val questionInstance = SavedQuestions(
                    id = randomId,
                    gameId,
                    state = "Unaswered",
                    difficulty = difficultylevel,
                    uniqueId = uniqueId,
                    content = randomQuestion,
                    questionText = randomQuestion.text
                )

                db.SavedQuestionsDao().insertQuestion(questionInstance)

                (questions as MutableList<Questions>).add(randomQuestion)
                allQuestions.remove(randomQuestion)

            }

            withContext(Dispatchers.Main) {
                updateQuestion()
            }


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