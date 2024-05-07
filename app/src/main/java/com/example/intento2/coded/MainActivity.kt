package com.example.intento2.coded

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.dataclass.GameSettings
import com.example.intento2.dataclass.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var db: MyAppDatabase
    private lateinit var textViewUserName: TextView
    private lateinit var textViewUserScore: TextView
    private lateinit var buttonPlay: Button

    private val USER_SELECTION_REQUEST_CODE = 1001



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = MyAppDatabase.getDatabase(applicationContext)

        textViewUserName = findViewById(R.id.textViewUserName)
        textViewUserScore = findViewById(R.id.textViewUserScore)

        lifecycleScope.launch {
            val users = db.userDao().getAllUsers()
            updateUI(users)
        }


        // Botones
        val usuariosbutton = findViewById<Button>(R.id.buttonChangeUser)
        usuariosbutton.setOnClickListener {
            val intent = Intent(this, UserSelectionActivity::class.java)
            startActivityForResult(intent, USER_SELECTION_REQUEST_CODE)
        }

        val recordbutton = findViewById<Button>(R.id.buttonHighScore)
        recordbutton.setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.buttonSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val buttonPlay = findViewById<Button>(R.id.buttonPlay)
        buttonPlay.setOnClickListener {
            checkPendingGame()
        }


    }

    private fun checkPendingGame() {
        lifecycleScope.launch {
            val activeUser = db.userDao().getActiveUserId()
            val user = db.userDao().getUserById(activeUser)

            if (user.pendingGame == true) {
                showResumeGamePrompt(user)
            } else {
                resetPendingGameStatus()
                navigateToJuegoActivity()
            }
        }
    }

    private fun resetPendingGameStatus() {
        GlobalScope.launch(Dispatchers.IO) {
            val activeUser = db.userDao().getActiveUser() ?: return@launch

            // Update pending game status to false
            activeUser.id?.let { db.userDao().disablePendingGameStatus(it) }

            // Delete all progress, questions, and answer options associated with the game
            // This depends on how your DAO methods are defined

            db.userDao().deleteAnswerOptionsByUserId(activeUser.id!!)
            db.userDao().deleteQuestionsByUserId(activeUser.id!!)
            db.userDao().deleteProgressByUserId(activeUser.id!!)
        }
    }


    private fun showResumeGamePrompt(user: User) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Resume Last Game?")
            .setMessage("Do you want to resume the last game?")
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                navigateToJuegoReanudadoActivity()
                dialogInterface.dismiss()
            }
            .setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                resetPendingGameStatus()
                navigateToJuegoActivity()
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToJuegoReanudadoActivity() {
        val intent = Intent(this, JuegoReanudado::class.java)
        startActivity(intent)
    }

    private fun navigateToJuegoActivity() {
        val intent = Intent(this, Juego::class.java)
        startActivity(intent)
    }

    private fun updateUI(users: List<User>) {
        if (users.isEmpty()) {
            showUserCreationPrompt()
        } else {
            val activeUser = users.find { it.active }
            activeUser?.let {
                val userName = it.name
                val userScore = it.highestScore ?: 0

                // Update UI components with user info
                textViewUserName.text = userName  // Update the user name TextView
                textViewUserScore.text = "Highest Score: $userScore"
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == USER_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reload users from the database and update the UI
            lifecycleScope.launch {
                val users = withContext(Dispatchers.IO) {
                    db.userDao().getAllUsers()
                }
                updateUI(users)
            }
        }
    }

    private fun showUserCreationPrompt() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_user_creation, null)
        dialogBuilder.setView(dialogView)

        dialogBuilder.setTitle("Create User")
        dialogBuilder.setMessage("Enter your nickname")

        dialogBuilder.setPositiveButton("Create") { dialog, which ->
            val nickname = dialogView.findViewById<EditText>(R.id.editTextNickname).text.toString()
            val newUser = User(id = 1, name = nickname, active = true)

            lifecycleScope.launch {
                db.userDao().insertUser(newUser)
                val users = db.userDao().getAllUsers()
                updateUI(users)
            }

            val defaultSettings = GameSettings.defaultInstance(newUser.id)
            lifecycleScope.launch {
                db.GameSettingsDao().insertGameSettings(defaultSettings)
            }
        }

        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
    }




}