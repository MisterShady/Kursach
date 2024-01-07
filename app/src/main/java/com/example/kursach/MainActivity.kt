package com.example.kursach

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.kursach.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("https://bsuedu.ru/bsu/education/schedule/")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }


    private val ScheduleApi by lazy {
        retrofit.create(ScheduleApi::class.java)
    }

    private val currentDateApi by lazy {
        currentDateRetrofit.create(CurrentDateApi::class.java)
    }

    private val currentDateRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://bsuedu.ru/bsu/education/schedule/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private lateinit var textDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        textDate = findViewById(R.id.textDate)

        loadCurrentDate()

        loadSchedule("12002108", "0801202414012024")
    }

    private fun loadCurrentDate() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = currentDateApi.getCurrentDate()
                if (response.isSuccessful) {
                    val currentDateList = response.body()
                    if (currentDateList != null && currentDateList.size >= 2) {
                        val currentDate = currentDateList[1] // Второй элемент массива

                        runOnUiThread {
                            textDate.text = currentDate
                        }
                    } else {
                        Log.e("CurrentDate", "Invalid response body format")
                    }
                } else {
                    Log.e("CurrentDate", "Error: ${response.code()}")
                    Log.e("CurrentDate", response.errorBody()?.string() ?: "Error body is null")
                }
            } catch (e: Exception) {
                Log.e("CurrentDate", "Failed to load current date", e)
            }
        }
    }



    private fun loadSchedule(group: String, week: String) {
        Log.d("Schedule", "Loading schedule for group: $group, week: $week")

        val call = ScheduleApi.loadSchedule(group, week)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.d("Schedule", "Response body: $responseBody")


                        val scheduleItems = parseHtmlForSchedule(responseBody)
                        updateScheduleUI(scheduleItems)
                    } catch (e: Exception) {
                        Log.e("Schedule", "Error parsing response", e)
                    }
                } else {
                    Log.e("Schedule", "Error: ${response.code()}")
                    Log.e("Schedule", response.errorBody()?.string() ?: "Error body is null")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Schedule", "Failed to load schedule", t)
            }
        })
    }



    private fun parseHtmlForSchedule(html: String?): List<ScheduleItem> {
        val scheduleItems = mutableListOf<ScheduleItem>()

        if (!html.isNullOrEmpty()) {
            val doc: Document = Jsoup.parse(html)
            Log.d("Schedule", "Parsed HTML: $doc")

            val rows = doc.select("tr:has(td)")

            var currentDay = ""
            for (row in rows) {
                try {
                    val columns = row.select("td")

                    if (columns.size == 1) {
                        // Это строка с названием дня
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

                        val scheduleItem =
                            ScheduleItem(num, time, lessonType, lesson, teacher, aud, currentDay)
                        scheduleItems.add(scheduleItem)
                    } else {
                        Log.e("Schedule", "Unexpected number of columns in a row: ${columns.size}")
                    }
                } catch (e: Exception) {
                    Log.e("Schedule", "Error parsing schedule item", e)
                }
            }
        }

        Log.d("Schedule", "Parsed schedule items: $scheduleItems")
        return scheduleItems
    }


    private fun updateScheduleUI(scheduleItems: List<ScheduleItem>) {
        val recyclerView: RecyclerView = binding.scheduleRecyclerView

        val adapter = ScheduleAdapter(scheduleItems)

        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
    }


}
