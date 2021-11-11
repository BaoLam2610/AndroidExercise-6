package com.example.musicappexercise6.model

import android.graphics.Bitmap
import java.io.Serializable

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumImage: Bitmap?,
    val path: String,
    val duration: Long
) : Serializable