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
import kotlinx.coroutines.launch


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
                //resetPendingGameStatus()
                navigateToJuegoActivity()
            }
        }
    }


    private fun showResumeGamePrompt(user: User) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Resume Last Game?")
            .setMessage("Do you want to resume the last game?")
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                //navigateToJuegoReanudadoActivity()
                dialogInterface.dismiss()
            }
            .setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                //resetPendingGameStatus()
                navigateToJuegoActivity()
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
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