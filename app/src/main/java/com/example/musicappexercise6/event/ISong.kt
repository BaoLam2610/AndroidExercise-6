package com.example.musicappexercise6.event

import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong

interface ISong {
    fun onShowSongList(songList: List<Song>)
    fun onEmptySongList()

    interface IFilterSong {
        fun onShowFilterSongs(filterSongList: List<FilterSong>)
    }

    interface IFavoriteSong {
        fun onShowFavSongs(favSongList: List<SongItem>)
    }

    interface IMySong {
        fun onShowMySongs(mySongList: List<SongItem>)
    }
}