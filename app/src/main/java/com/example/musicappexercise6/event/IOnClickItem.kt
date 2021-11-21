package com.example.musicappexercise6.event

import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong

interface IOnClickItem {
    fun onClickItemListener(id: String)

    interface ISongChart{
        fun onClickItemChartListener(song: Song)
    }

    interface ISongFilter{
        fun onClickItemFilterListener(song: FilterSong)
    }
}