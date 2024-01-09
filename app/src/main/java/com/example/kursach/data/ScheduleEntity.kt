package com.example.kursach.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "schedule_items")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = 0,
    val currentDay: String,
    val lesson: String,
    val note: String
)