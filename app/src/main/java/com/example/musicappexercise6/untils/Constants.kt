package com.example.musicappexercise6.untils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.model.related.Item
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.position
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import java.util.concurrent.TimeUnit

object Constants {
    const val BASE_URL = "http://mp3.zing.vn/"
    const val FILTER_URL =
        "http://ac.mp3.zing.vn/"//complete?type=artist,song,key,code&num=500&query="
    const val MUSIC_SHARED_PREFERENCES = "music_sp"
    const val SHARED_PREF_SHUFFLE = "sp_shuffle"
    const val SHARED_PREF_REPEAT_ONE = "sp_repeat_one"
    const val SHARED_PREF_REPEAT_ALL = "sp_repeat_all"
    const val EXTRA_SONG_POSITION = "position_song"
    const val EXTRA_TYPE = "type"
    const val CURRENT_SONG = "current_song"
    const val CHANNEL_ID = "channel_1"
    const val PLAY_PAUSE_SONG = "play_pause"
    const val NEXT_SONG = "next"
    const val PREV_SONG = "prev"
    const val CLOSE = "close"

    fun formattedTime(duration: Long): String {
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getImageSongFromPath(path: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        val albumImage = mmr.embeddedPicture
        if (albumImage != null) {
            return BitmapFactory.decodeByteArray(albumImage, 0, albumImage.size)
        }
        return null
    }

    fun setSongPosition(increment: Boolean) {
        if (increment) {
            if (position == songList.size - 1)
                position = 0
            else position++
        } else {
            if (position == 0)
                position = songList.size - 1
            else position--
        }
    }

    fun toSongItem(songChart: Song): SongItem {
        return SongItem(
            songChart.id,
            songChart.name,
            songChart.artists_names,
            songChart.thumbnail,
            songChart.type,
            songChart.duration
        )
    }

    fun toSongItem(item: Item): SongItem {
        return SongItem(
            item.id,
            item.name,
            item.artists_names,
            item.thumbnail,
            item.type,
            item.duration
        )
    }

    fun toSongItem(filter: FilterSong): SongItem{
        return SongItem(
            filter.id,
            filter.name,
            filter.artist,
            null,//filter.thumb,
            "audio",//type
            filter.duration.toInt()
        )
    }
}