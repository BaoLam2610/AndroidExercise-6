package com.example.musicappexercise6.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "song")
data class SongItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val artists_names: String,
    val thumbnail: String?,
    val type: String,
    val duration: Int
) : Serializable