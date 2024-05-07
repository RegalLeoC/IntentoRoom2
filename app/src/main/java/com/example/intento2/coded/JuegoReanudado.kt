package com.example.intento2.coded

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.HighScores
import com.example.intento2.dataclass.Options
import com.example.intento2.dataclass.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JuegoReanudado : AppCompatActivity() {

    private lateinit var buttonContainer: LinearLayout
    private lateinit var questionTextView: TextView
    private lateinit var topicImageView: ImageView
    private lateinit var questionNumberTextView: TextView
    private lateinit var hintTextView: TextView
    private lateinit var hintButton: Button

    private var questionOptions: MutableMap<Int, List<String>?> = mutableMapOf() // List of all options we generate initially per question
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

            topics = Topics.values()

            topics = Topics.updateTopics(
                gameSettings.topic1,
                gameSettings.topic2,
                gameSettings.topic3,
                gameSettings.topic4,
                gameSettings.topic5
            ).toTypedArray()


            for (i in 0 until numberOfQuestions) {
                answeredQuestions[i] = progress.answeredQuestions[i] ?: false
                answeredQuestionsHint[i] = progress.answeredQuestionsHint[i] ?: false
                userSelection[i] = progress.userSelection[i]
                hintSelection[i] = progress.hintSelection[i]
                questionOptions[i] = progress.questionOptions[i] as? List<String> ?: listOf()

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
        GlobalScope.launch(Dispatchers.IO){
            val questionsId = progress.gameId?.let { db.SavedQuestionsDao().getQuestionIdsByGameId(it) }
            questions = mutableListOf()

            Log.d("VictorEd", "Questions Id: $questionsId")

            if (questionsId != null) {
                for (i in questionsId) {
                    val question = db.SavedQuestionsDao().getQuestionContentById(i)
                    Log.d("VictorEd", "Question for id $i: $question")
                    question?.content?.let {
                        (questions as MutableList<Questions>).add(it)
                    }
                }
            }

            withContext(Dispatchers.Main){
                updateQuestion()
            }

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
                    createButtons(options!!)
                }

                withContext(Dispatchers.IO) {
                    progress.questionOptions[questionIndex] = options ?: mutableListOf()
                    db.progressDao().updateProgress(progress)

                }

            }



        } else {
            createButtons(questionOptions[questionIndex]!!)
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

    private fun createButtons(options: List<String>) {
        buttonContainer.removeAllViews()

        val isAnswered = answeredQuestions[questionIndex] ?: false
        val isAnsweredHint = answeredQuestionsHint[questionIndex] ?: false


        for (option in options) {
            val button = Button(this)
            button.text = option

            //Aqui poner la de navagacion de regreso para el hint (similar a lo dde arriba)
            val hintedButton = disabledWrongOptions[questionIndex]?.contains(option) ?: false

            val isSelected = userSelection[questionIndex] == option;

            //Checa si esta contestado
            // Si contestaste bien pues se pone verde, pero si contestaste mal, se pinta el rojo y se pone el verde mostrandote cual era el correcto
            if (isAnswered) {
                hintButton.isEnabled = false
                button.isEnabled = false
                val correctAnswer = questions[questionIndex].correctAnswer
                if (option == correctAnswer) {
                    button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))

                } else if (isSelected) {
                    button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))

                }

                if (hintedButton) {
                    button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                }

            }

            //Permite usar hint y navegar

            //Aqui poner lo de settins


            if(gameSettings.clues) {
                if (!isAnswered) {
                    hintButton.isEnabled = true
                    if (hintedButton) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                        button.isEnabled = false
                    }
                }


                // Navegacion si se contesto usando hints
                if (isAnsweredHint) {
                    hintButton.isEnabled = false

                    button.isEnabled = false
                    val correctAnswer = questions[questionIndex].correctAnswer

                    if (option == correctAnswer) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                    }

                    if (hintedButton) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                    }

                    disableButtons()

                }

            }


            // Aqui es cuanddo no haz contestado y se guarda en la lista de contestado manual




            button.setOnClickListener {
                if (!isAnswered) {
                    hintButton.isEnabled = false
                    val correctAnswer = questions[questionIndex].correctAnswer
                    if (option == correctAnswer) {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                        correctAnswers++ // Increment totalCorrectAnswers when the correct answer is selected
                        //Sumar
                        progress.correctAnswers = progress.correctAnswers?.plus(1)

                        // Use a coroutine to update the question state to "Correct"
                        //lifecycleScope.launch(Dispatchers.IO) {
                        val questionText = questions[questionIndex].text


                        /* activeUserId?.let { userId ->
                             lifecycleScope.launch(Dispatchers.IO) {
                                 db.progressDao()
                                     .updateCorrectAnswers(userId, correctAnswers ?: 0)
                             }
                         }

                         uniqueId?.let { it1 ->
                             db.questionDao().updateQuestionState(
                                 questionText, "Correct",
                                 it1
                             )

                             uniqueId?.let { it1 ->
                                 db.answerOptionDao().updateAnswerOptionState(
                                     option, "Selected",
                                     it1
                                 )
                             }


                         } */
                        //}

                        // Add one more hint if in hint streak
                        if (hintStreak == 1) {
                            hintsAvailable++
                            hintTextView.text = hintsAvailable.toString()
                            hintStreak = 0
                        } else {
                            hintStreak++
                        }


                        /*lifecycleScope.launch(Dispatchers.IO) {
                            uniqueId?.let { it1 ->
                                db.answerOptionDao().updateAnswerOptionState(
                                    option, "Selected",
                                    it1
                                )

                            }
                        } */


                    } else {
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                        hintStreak = 0

                        // Use a coroutine to update the question state to "Incorrect"
                        /*lifecycleScope.launch(Dispatchers.IO) {
                            val questionText = questions[questionIndex].text
                            uniqueId?.let { it1 ->
                                db.questionDao().updateQuestionState(
                                    questionText, "Incorrect",
                                    it1
                                )

                                uniqueId?.let { it1 ->
                                    db.answerOptionDao().updateAnswerOptionState(
                                        option, "Selected",
                                        it1
                                    )
                                }


                            }
                        }*/

                        // Show the correct answer by finding and coloring the button with the correct answer
                        for (i in 0 until buttonContainer.childCount) {
                            val child = buttonContainer.getChildAt(i)
                            if (child is Button && child.text.toString() == correctAnswer) {
                                child.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                            }
                        }

                        /*lifecycleScope.launch(Dispatchers.IO) {
                            uniqueId?.let { it1 ->
                                db.answerOptionDao().updateAnswerOptionState(
                                    option, "Selected",
                                    it1
                                )
                            }
                        } */


                    }

                    answeredQuestions[questionIndex] = true
                    progress.answeredQuestions[questionIndex] = true

                    userSelection[questionIndex] = option
                    progress.userSelection[questionIndex] = option

                    disableButtons() // Disable buttons after answering

                    // Check if we need to end the game
                    endGame()
                }
            }

            buttonContainer.addView(button)

        }

    }

    private fun endGame() {
        val totalAnswers = hintSelection.count { it.value != null } + userSelection.count { it.value != null }
        if (totalAnswers == numberOfQuestions) {
            viewResults()
        }
    }

    private fun viewResults() {
        // Calculate the final result based on the specified formula
        difficultyMultiplier = when (difficult) {
            "Easy" -> 1.0
            "Normal" -> 1.25
            "Hard" -> 1.5
            else -> 1.0
        }

        // Deduct points for hints
        val deduction = hintsUsed * 25  // Deduct 25 points for each hint used
        val totalpoints = correctAnswers * 100
        val finalResult = ((totalpoints - deduction) * difficultyMultiplier)

        // Fetch active user name asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            val activeUserName = activeUserId?.let { db.userDao().getUserNameById(it) }

            // Generate a random ID for HighScores
            val highScoresId = generateRandomHighScoresId()

            // Wait for the highScoresId to be generated
            val id = highScoresId ?: return@launch

            // Create HighScores instance when active user name is available
            activeUserName?.let { userName ->
                val highScores = HighScores(
                    id = id,
                    userId = activeUserId ?: 0, // Assuming activeUserId is not null
                    name = userName,
                    score = finalResult,
                    clues = cluesActive ?: false
                )

                // Save the HighScores instance in the database
                db.highScoresDao().insertHighScores(highScores)
            }

            db.userDao().setPendingGameToFalse(activeUserId)

        }

        // Pass the results to the FinPartida activity
        val intent = Intent(this, FinPartida::class.java)
        intent.putExtra("Deduction", deduction)
        intent.putExtra("totalScore", totalpoints)
        intent.putExtra("FinalResult", finalResult)
        intent.putExtra("difficultyMultiplier", difficultyMultiplier)

        startActivity(intent)
        finish()
    }

    private fun generateRandomHighScoresId(): Int? {
        var newId: Int
        do {
            newId = (0 until 1000).random()
        } while (db.highScoresDao().getHighScoresById(newId) != null)

        return newId
    }

    private fun disableButtons() {
        for (i in 0 until buttonContainer.childCount) {
            val child = buttonContainer.getChildAt(i)
            if (child is Button) {
                child.isEnabled = false
            }
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
        enabledWrongOptions[questionIndex] = wrongAnswers.toMutableList()
        progress.enabledWrongOptions[questionIndex] = wrongAnswers.toMutableList()

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