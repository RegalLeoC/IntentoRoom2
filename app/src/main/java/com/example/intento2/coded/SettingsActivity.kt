package com.example.intento2.coded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.GameSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var db: MyAppDatabase
    private var activeUserId: Int = 0
    private var gameSettings: GameSettings? = null
    private lateinit var seekBarQuestions: SeekBar
    private lateinit var switchHints: SwitchCompat
    private lateinit var checkBoxTheme1: CheckBox
    private lateinit var checkBoxTheme2: CheckBox
    private lateinit var checkBoxTheme3: CheckBox
    private lateinit var checkBoxTheme4: CheckBox
    private lateinit var checkBoxTheme5: CheckBox
    private lateinit var checkBoxTheme6: CheckBox
    private lateinit var spinnerDifficulty: Spinner
    private var lastChangedCheckBox: CheckBox? = null
    private lateinit var textViewNumQuestions: TextView
    private lateinit var buttonBackToMain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = MyAppDatabase.getDatabase(applicationContext)

        // Initialize UI components
        seekBarQuestions = findViewById(R.id.seekBarQuestions)
        switchHints = findViewById(R.id.switchHints)
        checkBoxTheme1 = findViewById(R.id.checkBoxTheme1)
        checkBoxTheme2 = findViewById(R.id.checkBoxTheme2)
        checkBoxTheme3 = findViewById(R.id.checkBoxTheme3)
        checkBoxTheme4 = findViewById(R.id.checkBoxTheme4)
        checkBoxTheme5 = findViewById(R.id.checkBoxTheme5)
        checkBoxTheme6 = findViewById(R.id.checkBoxTheme6)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        textViewNumQuestions = findViewById(R.id.textViewQuestions)
        buttonBackToMain = findViewById(R.id.buttonBackToMain)

        buttonBackToMain.setOnClickListener {
            navigateToMainActivity()
        }


        GlobalScope.launch(Dispatchers.IO) {
            activeUserId = db.userDao().getActiveUserId() ?: -1
            // Check if game settings exist for the active user
            checkGameSettings()
            // Set up UI components
            withContext(Dispatchers.Main) {
                setupUI()
            }
        }
    }

    private fun checkGameSettings() {
        GlobalScope.launch(Dispatchers.IO) {
            // Check if game settings exist for the active user
            gameSettings = db.GameSettingsDao().getGameSettingsByUserId(activeUserId)

            // If game settings don't exist, create new ones
            if (gameSettings == null) {
                var settingsId: Int? = null
                do {
                    val randomId = (1000..9999).random() // Generate random ID
                    val existingSettings = db.GameSettingsDao().getGameSettingsById(randomId)
                    if (existingSettings == null) {
                        // If settings with this ID doesn't exist, insert new settings with this ID
                        val newSettings = GameSettings(id = randomId, userId = activeUserId)
                        db.GameSettingsDao().insertGameSettings(newSettings)
                        settingsId = randomId
                    }
                } while (settingsId == null)

                // Retrieve the newly inserted game settings
                gameSettings = db.GameSettingsDao().getGameSettingsById(settingsId)
            }
        }
    }

    private fun setupUI() {
        gameSettings?.let { settings ->


            updateNumQuestionsTextView(seekBarQuestions.progress)

            // Set up SeekBar for selecting number of questions
            seekBarQuestions.progress = settings.numQuestions
            seekBarQuestions.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // Update number of questions in game settings when SeekBar progress changes
                    settings.numQuestions = progress
                    updateNumQuestionsTextView(progress)
                    updateSettingsInDatabase(settings)
                    checkSeekBarConstraints() // Check seek bar constraints
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // Set up Switch for enabling/disabling hints
            switchHints.isChecked = settings.clues
            switchHints.setOnCheckedChangeListener { _, isChecked ->
                // Update hints status in game settings when Switch state changes
                settings.clues = isChecked
                updateSettingsInDatabase(settings)
            }

            // Set up checkboxes for themes
            checkBoxTheme1.isChecked = settings.topic1
            checkBoxTheme1.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic1 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme1, isChecked)
            }

            checkBoxTheme2.isChecked = settings.topic2
            checkBoxTheme2.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic2 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme2, isChecked)
            }

            checkBoxTheme3.isChecked = settings.topic3
            checkBoxTheme3.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic3 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme3, isChecked)
            }

            checkBoxTheme4.isChecked = settings.topic4
            checkBoxTheme4.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic4 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme4, isChecked)
            }

            checkBoxTheme5.isChecked = settings.topic5
            checkBoxTheme5.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic5 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme5, isChecked)
            }

            checkBoxTheme6.isChecked = settings.topic6
            checkBoxTheme6.setOnCheckedChangeListener { _, isChecked ->
                gameSettings?.topic6 = isChecked
                updateGameSettings()
                updateCheckBoxState(checkBoxTheme6, isChecked)
            }

            // Set up spinner for difficulty
            val difficultyLevels = arrayOf("Easy", "Normal", "Hard")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyLevels)
            spinnerDifficulty.adapter = adapter
            val selectedIndex = difficultyLevels.indexOf(settings.difficulty)
            spinnerDifficulty.setSelection(selectedIndex)
            spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedDifficulty = difficultyLevels[position]
                    settings.difficulty = selectedDifficulty
                    updateSettingsInDatabase(settings)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            updateNumQuestionsTextView(settings.numQuestions)

            // Check constraints initially
            checkSeekBarConstraints() // Check seek bar constraints
            checkCheckboxConstraints() // Check checkbox constraints
        }
    }


    private fun checkSeekBarConstraints() {
        val maxQuestions = if (countSelectedTopics() == 1) 5 else Int.MAX_VALUE // Maximum number of questions based on selected topics
        if (seekBarQuestions.progress > maxQuestions) {
            showToast("Maximum number of questions must be $maxQuestions")
            seekBarQuestions.progress = maxQuestions
        }
    }

    private fun checkCheckboxConstraints() {
        val selectedTopicsCount = countSelectedTopics()
        if (selectedTopicsCount == 0) {
            showToast("Select at least one topic")
            lastChangedCheckBox?.isChecked = true // Revert the state of the last changed checkbox
        } else if (selectedTopicsCount == 1) {
            // Disable the last remaining checkbox
            disableLastRemainingCheckBox()
        } else {
            // Enable all checkboxes
            enableAllCheckBoxes()
        }
    }

    private fun disableLastRemainingCheckBox() {
        checkBoxTheme1.isEnabled = !checkBoxTheme1.isChecked
        checkBoxTheme2.isEnabled = !checkBoxTheme2.isChecked
        checkBoxTheme3.isEnabled = !checkBoxTheme3.isChecked
        checkBoxTheme4.isEnabled = !checkBoxTheme4.isChecked
        checkBoxTheme5.isEnabled = !checkBoxTheme5.isChecked
        checkBoxTheme6.isEnabled = !checkBoxTheme6.isChecked
    }

    private fun enableAllCheckBoxes() {
        checkBoxTheme1.isEnabled = true
        checkBoxTheme2.isEnabled = true
        checkBoxTheme3.isEnabled = true
        checkBoxTheme4.isEnabled = true
        checkBoxTheme5.isEnabled = true
        checkBoxTheme6.isEnabled = true
    }


    private fun updateLastChangedCheckBox(checkBox: CheckBox) {
        lastChangedCheckBox = checkBox
        checkCheckboxConstraints()
    }

    private fun updateCheckBoxState(checkBox: CheckBox, isChecked: Boolean) {
        checkBox.isChecked = isChecked
        updateLastChangedCheckBox(checkBox)
    }




    private fun countSelectedTopics(): Int {
        var count = 0
        gameSettings?.let { settings ->
            if (settings.topic1) count++
            if (settings.topic2) count++
            if (settings.topic3) count++
            if (settings.topic4) count++
            if (settings.topic5) count++
            if (settings.topic6) count++
        }
        return count
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun updateNumQuestionsTextView(numQuestions: Int) {
        textViewNumQuestions.text = "Number of Questions: $numQuestions"
    }


    private fun updateSettingsInDatabase(settings: GameSettings) {
        GlobalScope.launch(Dispatchers.IO) {
            db.GameSettingsDao().updateGameSettings(settings)
        }
    }

    private fun updateGameSettings() {
        GlobalScope.launch(Dispatchers.IO) {
            gameSettings?.let {
                db.GameSettingsDao().updateGameSettings(it)
            }
        }
    }

    private fun navigateToMainActivity() {
        finish() // Finish the SettingsActivity to return to the previous activity (Main)
    }

}

