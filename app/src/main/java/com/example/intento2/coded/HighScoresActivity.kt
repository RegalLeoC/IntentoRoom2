package com.example.intento2.coded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.intento2.R
import com.example.intento2.database.MyAppDatabase
import com.example.intento2.recyclerview.HighScoresAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HighScoresActivity : AppCompatActivity() {

    private lateinit var highScoresRecyclerView: RecyclerView
    private lateinit var highScoresAdapter: HighScoresAdapter
    private lateinit var db: MyAppDatabase
    private lateinit var returnButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        // Initialize RecyclerView
        highScoresRecyclerView = findViewById(R.id.highScoresRecyclerView)
        highScoresRecyclerView.layoutManager = LinearLayoutManager(this)
        highScoresAdapter = HighScoresAdapter(emptyList()) // Initialize with an empty list
        highScoresRecyclerView.adapter = highScoresAdapter

        // Initialize database
        db = MyAppDatabase.getDatabase(applicationContext)

        // Load high scores from database and update RecyclerView
        loadHighScores()

        returnButton = findViewById(R.id.returnButton)
        returnButton.setOnClickListener {
            finish()
        }

    }

    private fun loadHighScores() {
        GlobalScope.launch(Dispatchers.IO) {
            val highScoresList = db.highScoresDao().getTop20HighScores() // Get top 20 high scores from the database
            runOnUiThread {
                highScoresAdapter.setData(highScoresList) // Update RecyclerView with high scores data
            }
        }
    }
}
