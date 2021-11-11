package com.example.musicappexercise6.event

import com.example.musicappexercise6.model.Song

interface ISong {
    fun onShowSongList(songList: MutableList<Song>)
    fun onEmptySongList()
}