package com.example.musicappexercise6.model.chart

data class Album(
    val artists: List<ArtistX>,
    val artists_names: String,
    val id: String,
    val isoffical: Boolean,
    val link: String,
    val name: String,
    val thumbnail: String,
    val thumbnail_medium: String,
    val title: String
)