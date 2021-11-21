package com.example.musicappexercise6.event

import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.model.chart.Song

interface ISong {
    fun onShowSongList(songList: List<Song>)
    fun onEmptySongList()

    interface IFilterSong{
        fun onShowFilterSongs(filterSongList: List<FilterSong>)
    }
}