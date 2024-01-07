package com.example.kursach

import retrofit2.Response
import retrofit2.http.GET

interface CurrentDateApi {
    @GET("week/week_json.php")
    suspend fun getCurrentDate(): Response<List<String>>
}

