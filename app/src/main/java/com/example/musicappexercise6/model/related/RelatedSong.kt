package com.example.musicappexercise6.model.related

data class RelatedSong(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)