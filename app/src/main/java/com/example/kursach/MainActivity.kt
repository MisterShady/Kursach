package com.example.kursach

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("https://bsuedu.ru/bsu/education/schedule/")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }


    private val ScheduleApi by lazy {
        retrofit.create(ScheduleApi::class.java)
    }


    private lateinit var textDate: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        textDate = findViewById(R.id.textDate)

        // Установим начальное значение для textDate при запуске
        val currentDateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        val endCalendar = Calendar.getInstance()
        endCalendar.add(Calendar.DATE, 6)
        val endDateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endCalendar.time)
        textDate.text = "$currentDateString - $endDateString"

        loadSchedule("12002108")

        val iconLeft: ImageView = findViewById(R.id.iconLeft)
        val iconRight: ImageView = findViewById(R.id.iconRight)

        iconLeft.setOnClickListener {
            // Обработчик клика по стрелке влево
            loadNextWeek(false)
        }

        iconRight.setOnClickListener {
            // Обработчик клика по стрелке вправо
            loadNextWeek(true)
        }
    }

    private fun loadNextWeek(isNext: Boolean) {
        val formatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault())

        try {
            val startDate = formatter.parse(currentWeek.substring(0, 8))
            val endDate = formatter.parse(currentWeek.substring(8))

            val startCalendar = Calendar.getInstance()
            startCalendar.time = startDate

            val endCalendar = Calendar.getInstance()
            endCalendar.time = endDate

            if (isNext) {
                startCalendar.add(Calendar.DATE, 7)
                endCalendar.add(Calendar.DATE, 7)
            } else {
                startCalendar.add(Calendar.DATE, -7)
                endCalendar.add(Calendar.DATE, -7)
            }

            val newStartDate = formatter.format(startCalendar.time)
            val newEndDate = formatter.format(endCalendar.time)

            currentWeek = "$newStartDate$newEndDate"

            // Обновляем значение textDate
            val currentDateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startCalendar.time)
            val endDateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endCalendar.time)

            textDate.text = "$currentDateString - $endDateString"

            // Добавьте следующую строку для вывода значения недели в Logcat
            Log.d("WeekSwitch", "Switched to week: $currentWeek")

            loadSchedule("12002108")
        } catch (e: ParseException) {
            Log.e("WeekSwitch", "Error parsing date", e)
        }
    }

    private var currentWeek: String = "0101202407012024" // Начальное значение

    private fun loadSchedule(group: String) {

        val call = ScheduleApi.loadSchedule(group, currentWeek)
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

        if (scheduleItems.isEmpty()) {
            // Если расписание пусто, выводим сообщение
            val noClassesTextView: TextView = binding.noClassesTextView
            noClassesTextView.visibility = View.VISIBLE

            // Скрываем RecyclerView
            recyclerView.visibility = View.GONE
        } else {
            // Если есть занятия, отображаем их в RecyclerView
            val adapter = ScheduleAdapter(scheduleItems)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            // Скрываем сообщение о отсутствии занятий
            val noClassesTextView: TextView = binding.noClassesTextView
            noClassesTextView.visibility = View.GONE

            // Показываем RecyclerView
            recyclerView.visibility = View.VISIBLE
        }
    }



}
