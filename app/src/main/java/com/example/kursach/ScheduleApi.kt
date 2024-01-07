package com.example.kursach

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ScheduleApi {

    @FormUrlEncoded
    @POST("groups/show_schedule.php")
    fun loadSchedule(
        @Field("group") group: String,
        @Field("week") week: String,
        @Field("fak") fak: String = "",
        @Field("frm") frm: String = ""
    ): Call<ResponseBody>
}
