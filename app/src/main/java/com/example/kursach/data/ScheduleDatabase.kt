package com.example.kursach.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScheduleEntity::class], version = 2, exportSchema = false)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao

    companion object {

        @Volatile
        private var instance: ScheduleDatabase? = null

        fun getInstance(context: Context): ScheduleDatabase = instance ?: synchronized(this) {
            instance ?: getBuiltDatabase(context).also {
                instance = it
            }
        }

        private fun getBuiltDatabase(context: Context): ScheduleDatabase = Room.databaseBuilder(
            context, ScheduleDatabase::class.java, "cardDatabase"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}