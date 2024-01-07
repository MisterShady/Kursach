package com.example.kursach

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DateAdapter(private val dateItems: List<DateItem>) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currentDayTextView: TextView = itemView.findViewById(R.id.currentDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.date_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dateItems[position]
        holder.currentDayTextView.text = item.currentDay
    }

    override fun getItemCount(): Int {
        return dateItems.size
    }
}
