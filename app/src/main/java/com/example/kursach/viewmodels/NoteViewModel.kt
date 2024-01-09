package com.example.kursach.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.kursach.data.ScheduleDao
import com.example.kursach.data.ScheduleDatabase
import com.example.kursach.data.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(
    private val scheduleDao: ScheduleDao, val currentDay: String, val lesson: String
) : ViewModel() {

    var scheduleItem = MutableLiveData<ScheduleEntity>()

    init {
        val itemFromDb = scheduleDao.getNoteForScheduleItem(currentDay, lesson)
        if (itemFromDb == null) {
            scheduleItem.postValue(ScheduleEntity(null, currentDay, lesson, ""))
        } else {
            scheduleItem.postValue(scheduleDao.getNoteForScheduleItem(currentDay, lesson))
        }
    }

    fun saveScheduleItem(note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduleDao.insertScheduleEntity(
                ScheduleEntity(
                    null, currentDay, lesson, note
                )
            )
        }

    }

    companion object {

        fun Factory(currentDay: String, lesson: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>, extras: CreationExtras
                ): T {
                    val application =
                        checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    return NoteViewModel(
                        ScheduleDatabase.getInstance(application).scheduleDao(), currentDay, lesson
                    ) as T
                }
            }
    }
}