package com.example.musicappexercise6.model.chart

data class SongHis(
    val `data`: DataX,
    val from: Long,
    val interval: Int,
    val max_score: Float,
    val min_score: Float,
    val score: Score,
    val total_score: Int
)