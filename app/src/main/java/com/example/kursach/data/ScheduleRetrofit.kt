package com.example.kursach.data


import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ScheduleRetrofit {

    @FormUrlEncoded
    @POST("groups/show_schedule.php")
    suspend fun loadSchedule(
        @Field("group") group: String,
        @Field("week") week: String,
        @Field("fak") fak: String = "",
        @Field("frm") frm: String = ""
    ): ResponseBody

    companion object {

        fun getInstance(): ScheduleRetrofit =
            Retrofit.Builder().baseUrl("https://bsuedu.ru/bsu/education/schedule/").addConverterFactory(GsonConverterFactory.create())
                .build().create(ScheduleRetrofit::class.java)
    }
}