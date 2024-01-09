package com.example.kursach.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleEntity(scheduleEntity: ScheduleEntity)

    @Query("SELECT * FROM schedule_items WHERE lesson = :lesson AND currentDay = :currentDay")
    fun getNoteForScheduleItem(currentDay: String, lesson: String): ScheduleEntity?
}