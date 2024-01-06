package com.example.kursach

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(private val scheduleItems: List<ScheduleItem>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Определите TextView здесь, например:
        val numTextView: TextView = itemView.findViewById(R.id.num)
        val timeTextView: TextView = itemView.findViewById(R.id.time)
        val typeTextView: TextView = itemView.findViewById(R.id.lessonType)
        val subjectTextView: TextView = itemView.findViewById(R.id.lesson)
        val teacherTextView: TextView = itemView.findViewById(R.id.teacher)
        val audTextView: TextView = itemView.findViewById(R.id.aud)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleItems[position]

        // Устанавливаем значения в TextView из данных
        holder.numTextView.text = item.num
        holder.timeTextView.text = item.time
        holder.typeTextView.text = item.lessonType
        holder.subjectTextView.text = item.lesson
        holder.teacherTextView.text = item.teacher
        holder.audTextView.text = item.aud
    }

    override fun getItemCount(): Int {
        return scheduleItems.size
    }
}
