package com.example.intento2.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.intento2.R
import com.example.intento2.dataclass.HighScores

class HighScoresAdapter(private val context: List<Any>) : RecyclerView.Adapter<HighScoresAdapter.HighScoresViewHolder>() {

    private var highScoresList: MutableList<HighScores> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoresViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.high_scores_item, parent, false)
        return HighScoresViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HighScoresViewHolder, position: Int) {
        val currentItem = highScoresList[position]
        holder.userNameTextView.text = currentItem.name
        holder.scoreTextView.text = currentItem.score.toString()
        holder.cluesUsedTextView.text = if (currentItem.clues == true) "Yes" else "No"
    }

    override fun getItemCount(): Int {
        return highScoresList.size
    }

    fun setData(newList: List<HighScores>) {
        highScoresList.clear()
        highScoresList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class HighScoresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val cluesUsedTextView: TextView = itemView.findViewById(R.id.cluesUsedTextView)
    }
}

