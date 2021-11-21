package com.example.musicappexercise6.model

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SongItem(
    val id: String,
    val name: String,
    val artists_names: String,
    val thumbnail: String?,
    val type: String,
    val duration: Int
) : Serializable