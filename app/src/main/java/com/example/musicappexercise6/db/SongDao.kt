package com.example.musicappexercise6.db

import androidx.room.*
import com.example.musicappexercise6.model.SongItem

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(song: SongItem): Long

    @Query("SELECT * FROM song")
    fun getAllSongFavorite(): List<SongItem>

    @Delete
    suspend fun deleteSong(song: SongItem)
}