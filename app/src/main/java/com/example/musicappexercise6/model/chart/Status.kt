package com.example.musicappexercise6.model.chart

data class Status(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)