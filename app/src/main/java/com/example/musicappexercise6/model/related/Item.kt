package com.example.musicappexercise6.model.related

import com.google.gson.annotations.SerializedName

data class Item(
    val artists: List<Artist>,
    val artists_names: String,
    val code: String,
    val content_owner: Int,
    val duration: Int,
    val id: String,
    val isWorldWide: Boolean,
    @SerializedName("isoffical")
    val isOffical: Boolean,
    val link: String,
    val lyric: String,
    val name: String,
    val order: Any,
    val performer: String,
    val playlist_id: String,
    val thumbnail: String,
    val title: String,
    val type: String
)