package com.example.musicappexercise6.model.chart

data class Customied(
    val artists: List<Artist>,
    val artists_names: String,
    val code: String,
    val content_owner: Int,
    val duration: Int,
    val id: String,
    val isWorldWide: Boolean,
    val isoffical: Boolean,
    val link: String,
    val lyric: String,
    val name: String,
    val performer: String,
    val playlist_id: String,
    val thumbnail: String,
    val title: String,
    val type: String
)