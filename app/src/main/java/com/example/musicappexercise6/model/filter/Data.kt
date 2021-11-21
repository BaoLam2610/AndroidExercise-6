package com.example.musicappexercise6.model.filter

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("song")
    val filterSong: List<FilterSong>
)