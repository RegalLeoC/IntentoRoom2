package com.example.intento2.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.intento2.R
import com.example.intento2.dataclass.User
//import com.example.roomtest.R
//import com.example.roomtest.dataclass.User

class UserSelectionAdapter(private val userList: List<User>, private val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<UserSelectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSelectionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_selection, parent, false)
        return UserSelectionViewHolder(itemView, onItemClick)
    }

    override fun onBindViewHolder(holder: UserSelectionViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.bind(currentUser)
        //holder.textViewUserName.text = currentUser.name
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
