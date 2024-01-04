package com.example.kursach

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.kursach.databinding.ActivityMainBinding
import okhttp3.ResponseBody


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bsuedu.ru/bsu/education/schedule/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // При создании активности загружаем расписание для группы "12002108" и недели "0801202414012024"
        loadSchedule("12002108", "0801202414012024")
    }

    private fun loadSchedule(group: String, week: String) {
        Log.d("Schedule", "Loading schedule for group: $group, week: $week")

        val call = apiService.loadSchedule(group, week)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        Log.d("Schedule", "Response body: $responseBody")

                        // Парсинг HTML с использованием jsoup
                        val scheduleItems = parseHtmlForSchedule(responseBody)
                        updateScheduleUI(scheduleItems)
                    } catch (e: Exception) {
                        Log.e("Schedule", "Error parsing response", e)
                    }
                } else {
                    // Обработка ошибки
                    Log.e("Schedule", "Error: ${response.code()}")
                    Log.e("Schedule", response.errorBody()?.string() ?: "Error body is null")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Обработка ошибки
                Log.e("Schedule", "Failed to load schedule", t)
            }
        })
    }

    private fun parseHtmlForSchedule(html: String?): List<ScheduleItem> {
        val scheduleItems = mutableListOf<ScheduleItem>()

        if (!html.isNullOrEmpty()) {
            // Используем jsoup для парсинга HTML
            val doc: Document = Jsoup.parse(html)
            Log.d("Schedule", "Parsed HTML: $doc")

            // Ищем все элементы с указанными классами
            val rows = doc.select("tr:has(td)")

            for (row in rows) {
                val columns = row.select("td")

                if (columns.size == 6) { // Убедимся, что у нас достаточно столбцов для пары
                    val num = columns[0].text()
                    val time = columns[1].text()
                    val lessonType = columns[2].text()
                    val lesson = columns[3].text()
                    val teacher = columns[4].text()
                    val aud = columns[5].text()

                    val subject = "$lessonType $lesson"
                    val scheduleItem = ScheduleItem(subject, teacher, aud, time)
                    scheduleItems.add(scheduleItem)
                }
            }
        }

        return scheduleItems
    }


    private fun updateScheduleUI(scheduleItems: List<ScheduleItem>) {
        // Очищаем текущий текст
        binding.scheduleTextView.text = ""

        // Добавляем новые элементы
        for (item in scheduleItems) {
            val displayText = "${item.subject} - ${item.teacher} - ${item.room} - ${item.time}"
            binding.scheduleTextView.append("$displayText\n")
        }
    }
}
