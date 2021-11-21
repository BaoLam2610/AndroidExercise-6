package com.example.musicappexercise6.api

import com.example.musicappexercise6.model.filter.FilterStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FilterAPI {
    @GET("complete?type=artist,song,key,code")
    suspend fun getSongFilter(
        @Query("num") num: Int,
        @Query("query") filter: String
    ): Response<FilterStatus>
}