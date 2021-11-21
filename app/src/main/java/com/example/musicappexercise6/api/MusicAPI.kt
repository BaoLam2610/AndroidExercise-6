package com.example.musicappexercise6.api

import com.example.musicappexercise6.model.chart.Status
import com.example.musicappexercise6.model.related.RelatedSong
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicAPI {

    @GET("xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun getSongChart(): Response<Status>

    @GET("xhr/recommend?type=audio")
    suspend fun getSongRelated(
        @Query("id") id: String
    ): Response<RelatedSong>


}