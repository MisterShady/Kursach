package com.example.kursach

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MainViewModel(private val retrofit: ScheduleRetrofit) : ViewModel() {

    private var _subjectItems = MutableLiveData<List<SubjectItem>>()
    val subjectItems: LiveData<List<SubjectItem>> = _subjectItems

    fun loadSchedule(group: String, week: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val responseBody = retrofit.loadSchedule(group, week).string()
            _subjectItems.postValue(parseHtmlForSchedule(responseBody))
        }
    }

    private fun parseHtmlForSchedule(html: String?): List<SubjectItem> {
        val subjectItems = mutableListOf<SubjectItem>()

        if (!html.isNullOrEmpty()) {
            val doc: Document = Jsoup.parse(html)
            Log.d("Schedule", "Parsed HTML: $doc")

            val rows = doc.select("tr:has(td)")

            var currentDay = ""
            for (row in rows) {
                try {
                    val columns = row.select("td")

                    if (columns.size == 1) {
                        val headerDay = columns.select("span.dnmrt span.h3 b")
                        if (headerDay.isNotEmpty()) {
                            currentDay = headerDay.text().trim()
                        }
                    } else if (columns.size == 6) {
                        val num = columns[0].text()
                        val time = columns[1].text()
                        val lessonType = columns[2].text()
                        val lesson = columns[3].text()
                        val teacher = columns[4].text()
                        val aud = columns[5].text()

                        val subjectItem =
                            SubjectItem(num, time, lessonType, lesson, teacher, aud)
                        subjectItems.add(subjectItem)
                    } else {
                        Log.e("Schedule", "Unexpected number of columns in a row: ${columns.size}")
                    }
                } catch (e: Exception) {
                    Log.e("Schedule", "Error parsing schedule item", e)
                }
            }
        }

        return subjectItems
    }

    companion object {

        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                return MainViewModel(ScheduleRetrofit.getInstance()) as T
            }
        }
    }
}