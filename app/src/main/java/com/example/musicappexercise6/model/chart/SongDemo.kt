package com.example.musicappexercise6.model.chart

import com.google.gson.annotations.SerializedName

data class SongDemo(
    val id: String,
    val name: String,
    val title: String,
    val code: String,
    @SerializedName("artists_names")
    val artistsNames: String,
    val type: String,
    val thumbnail: String,
    val duration: Int,
    val position: Int
)