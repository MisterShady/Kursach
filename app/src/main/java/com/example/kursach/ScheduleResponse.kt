package com.example.kursach

data class ScheduleResponse(
    val group: String,
    val week: String,
    val schedule: List<ScheduleItem>
)
