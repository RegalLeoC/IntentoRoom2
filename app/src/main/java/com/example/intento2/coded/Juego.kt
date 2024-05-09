package com.example.intento2.coded

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.HighScores
import com.example.intento2.dataclass.Options
import com.example.intento2.dataclass.Progress
import com.example.intento2.dataclass.SavedQuestions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private lateinit var progress: Progress
    private lateinit var gameSettings: GameSettings
    private var backPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        // Initialize layout after database operations
        buttonContainer = findViewById(R.id.buttonContainer)
        questionTextView = findViewById(R.id.questionTextView)
        topicImageView = findViewById(R.id.topicImageView)
        questionNumberTextView = findViewById(R.id.questionNumberTextView)
        hintTextView = findViewById(R.id.hintTextView)
        hintButton = findViewById(R.id.hintButton)

        db = MyAppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.Main) {
            uniqueId = generateUniqueId()
            gameId = generateRandomGameId()
            activeUserId = getActiveUserId()
            gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)

            settingsId = gameSettings.id
            cluesActive = gameSettings.clues
            difficult = gameSettings.difficulty
            numberOfQuestions = gameSettings.numQuestions

            if(gameSettings.clues == false){
                hintButton.isVisible = false
                hintButton.isEnabled = false
            }

            progress = Progress(
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
                gameSettings.topic5,
                gameSettings.topic6
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




            setUpClickListeners()

            // Select random questions and update UI
            selectRandomQuestions()

        }

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
                useHint()
            }
        }
    }

    private fun useHint() {

        //check if we have hints remaining
        if(hintsAvailable > 0) {

            //Update variables
            hintsAvailable--
            hintStreak = 0
            hintsUsed++

            //Update in progress
            progress.hintsAvailable = hintsAvailable
            progress.hintsUsed = hintsUsed
            progress.hintStreak = hintStreak

            // Update text
            hintTextView.text = hintsAvailable.toString()

            //Se inicilizan las variables para apuntar a la prgunta actual y sus opciones incorrectas
            val currentQuestion = questions[questionIndex]
            val enabledOptions = enabledWrongOptions[questionIndex]

            // Se selecciona al azar una opcion para deshabilitar con la pista
            if((enabledOptions?.size ?:0) > 1){
                val randomIndex = (0 until enabledOptions!!.size).random()
                val optionToDisable = enabledOptions[randomIndex]

                // Remove from enabled options and add it to disabled options
                enabledWrongOptions[questionIndex]?.remove(optionToDisable)
                disabledWrongOptions[questionIndex] = (disabledWrongOptions[questionIndex]?: emptyList()) + optionToDisable


                progress.enabledWrongOptions = enabledWrongOptions
                progress.disabledWrongOptions = disabledWrongOptions


                disableOption(optionToDisable)

            } else {
                // Si solo queda una opcion mala la pregunta se contesta sola
                val correctAnswer = currentQuestion.correctAnswer
                for(i in 0 until buttonContainer.childCount){
                    val child = buttonContainer.getChildAt(i)
                    if (child is Button) {
                        val option = child.text.toString()
                        if (option != correctAnswer && enabledOptions!!.contains(option)) {
                            disabledWrongOptions[questionIndex] = (disabledWrongOptions[questionIndex]?: emptyList()) + option
                            progress.disabledWrongOptions = disabledWrongOptions

                            disableOption(option)
                        } else if (option == correctAnswer) {
                            correctAnswers++
                            progress.correctAnswers = progress.correctAnswers?.plus(1)
                            // Necesito contar estos mas el userSelection para terminarlo
                            hintSelection[questionIndex] = option
                            progress.hintSelection[questionIndex] = option

                            answeredQuestionsHint[questionIndex] = true
                            progress.answeredQuestionsHint[questionIndex] = true

                            child.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                            //Igual deshabilita el hint button????
                            disableButtons()



                            //Checar si ya hay que terminar el juego
                            endGame()
                        }

                    }

                }


            }


        }

        lifecycleScope.launch(Dispatchers.IO) {
            db.progressDao().updateProgress(progress)
        }

    }

    private fun disableOption(option: String) {
        for (i in 0 until buttonContainer.childCount) {
            val child = buttonContainer.getChildAt(i)
            if (child is Button && child.text.toString() == option) {
                child.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                child.isEnabled = false
            }
        }
    }

    private fun nextQuestion() {
        questionIndex = (questionIndex + 1) % numberOfQuestions
        progress.questionIndex = questionIndex
        updateQuestion()
        //updateNavigationBar()
    }

    private fun previousQuestion() {
        questionIndex = (questionIndex - 1 + questions.size) % numberOfQuestions
        progress.questionIndex = questionIndex
        updateQuestion()
        //updateNavigationBar()
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
                            progress.hintsAvailable = hintsAvailable
                            progress.hintStreak = hintStreak
                        } else {
                            hintStreak++
                            progress.hintStreak = hintStreak
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
                        progress.hintStreak = hintStreak

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


                    lifecycleScope.launch(Dispatchers.IO) {
                        db.progressDao().updateProgress(progress)
                    }

                    disableButtons() // Disable buttons after answering

                    // Check if we need to end the game
                    endGame()
                }
            }

            lifecycleScope.launch(Dispatchers.IO) {
                db.progressDao().updateProgress(progress)
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

    var indexNum: Int = 0
    private fun selectRandomQuestions() {

        GlobalScope.launch(Dispatchers.IO) {

            val gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)

            val allQuestions = topics.flatMap { it.questions }.toMutableList()
            questions = mutableListOf()

            repeat(numberOfQuestions) {
                val randomQuestion = allQuestions.random()
                val randomId = generateRandomQuestionId()


                val difficultylevel = gameSettings.difficulty
                val questionInstance = SavedQuestions(
                    id = randomId,
                    gameId,
                    state = "Unaswered",
                    difficulty = difficultylevel,
                    uniqueId = uniqueId,
                    content = randomQuestion,
                    questionText = randomQuestion.text,
                    indexNum
                )

                db.SavedQuestionsDao().insertQuestion(questionInstance)

                indexNum++

                (questions as MutableList<Questions>).add(randomQuestion)
                allQuestions.remove(randomQuestion)

            }

            withContext(Dispatchers.Main) {
                updateQuestion()
            }


        }

    }

    private fun generateRandomQuestionId(): Int {
        var newId: Int
        do {
            newId = (0 until 1000).random()
        } while (db.SavedQuestionsDao().getQuestionById(newId) != null)
        return newId
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


    override fun onBackPressed() {
        // Check if the activity is finishing
        if (!isFinishing) {
            lifecycleScope.launch {
                // Update progress before exiting
                updateProgressBeforeExit()

                // Build the confirmation dialog
                val alertDialogBuilder = AlertDialog.Builder(this@Juego)
                alertDialogBuilder.setMessage("Are you sure you want to leave?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Finish the activity if the user confirms
                        dialog.dismiss()
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        // Dismiss the dialog if the user cancels
                        dialog.cancel()
                    }

                // Create and show the dialog only if the activity is not finishing
                if (!isFinishing) {
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
        } else {
            super.onBackPressed()
        }
    }



    private suspend fun updateProgressBeforeExit() {
        // Update progress here before exiting
        // For example:
        withContext(Dispatchers.IO) {
            // Update progress in the database
            db.progressDao().updateProgress(progress)
        }
    }


    /*
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            // Start JuegoReanudado activity for landscape orientation
            val intent = Intent(this, JuegoReanudado::class.java)
            startActivity(intent)
            finish() // Finish current activity
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            // Start JuegoReanudado activity for portrait orientation
            val intent = Intent(this, JuegoReanudado::class.java)
            startActivity(intent)
            finish() // Finish current activity
        }
    } */



}