package com.example.musicappexercise6.presenter

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.example.musicappexercise6.api.RetrofitInstance
import com.example.musicappexercise6.db.SongDatabase
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.untils.Constants.MUSIC_SHARED_PREFERENCES
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ALL
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ONE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_SHUFFLE
import com.example.musicappexercise6.untils.Constants.getImageSongFromPath
import com.example.musicappexercise6.untils.Constants.toSongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongPresenter {

    var context: Context? = null
    var iSong: ISong? = null
    var iFilterSong: ISong.IFilterSong? = null
    var iFavSong: ISong.IFavoriteSong? = null
    var iMySong: ISong.IMySong? = null

    constructor(context: Context, iSong: ISong, iFilterSong: ISong.IFilterSong) {
        this.context = context
        this.iSong = iSong
        this.iFilterSong = iFilterSong
        sharedPref = context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
    }

    constructor(context: Context, iFavoriteSong: ISong.IFavoriteSong) {
        this.context = context
        this.iFavSong = iFavoriteSong
        sharedPref = context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
    }

    constructor(context: Context, iMySong: ISong.IMySong) {
        this.context = context
        this.iMySong = iMySong
        sharedPref = context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
    }

    companion object {
        var sharedPref: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null
        var isShuffle = sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) ?: false
        var isRepeatOne = sharedPref?.getBoolean(SHARED_PREF_REPEAT_ONE, false) ?: false
        var isRepeatAll = sharedPref?.getBoolean(SHARED_PREF_REPEAT_ALL, true) ?: true
    }

    fun showMySongList() {
        var songList = mutableListOf<SongItem>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val uriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val uriInternal = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val cursor: Cursor? = context!!.contentResolver.query(uriExternal,
            projection,
            selection,
            null,
            null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val song = SongItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),//cursor.getString(3),// image
                    cursor.getString(4),//cursor.getString(3),// image
                    cursor.getLong(5).toInt()/1000
                )
                songList.add(song)
            }
        }
        if(songList.isNotEmpty())
            iMySong!!.onShowMySongs(songList)
    }

    fun getSongChartFromApi() {
        GlobalScope.launch(Dispatchers.IO) {
            val call = RetrofitInstance.api.getSongChart()
            Log.d(MainActivity.TAG, call.code().toString())
            if (call.isSuccessful) {
//                val songChartList = call.body()
//                iSong!!.onShowSongList(songChartList as MutableList<SongItem>)
                val status = call.body()
                val data = status?.data
                val songList = data?.song
                if (songList != null) {
                    withContext(Dispatchers.Main) {
                        iSong!!.onShowSongList(songList)
                    }
                }
            }
        }
    }

    fun getSongListRelated(songChart: Song?, filterSong: FilterSong?): MutableList<SongItem> {
        var songItems = mutableListOf<SongItem>()
        if (songChart != null)
            songItems.add(toSongItem(songChart))
        if (filterSong != null)
            songItems.add(toSongItem(filterSong))
        GlobalScope.launch(Dispatchers.IO) {
            val call = RetrofitInstance.api.getSongRelated(
                when {
                    songChart != null -> songChart.id
                    filterSong != null -> filterSong.id
                    else -> ""
                }
            )
            val status = call.body()
            val data = status?.data
            val songList = data?.items
            if (songList != null) {
                for (item in songList) {
                    songItems.add(toSongItem(item))
                }
            }
        }
        return songItems
    }

    private fun getFilterSongFromApi(s: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val call = RetrofitInstance.apiFilter.getSongFilter(500, s)
            if (call.isSuccessful) {
//                val songChartList = call.body()
//                iSong!!.onShowSongList(songChartList as MutableList<SongItem>)
                val status = call.body()
                val data = status?.data
                val songList = data?.get(0)?.filterSong
                if (songList != null) {
                    withContext(Dispatchers.Main) {
                        try {
                            iFilterSong!!.onShowFilterSongs(songList)
                        } catch (e: Exception){

                        }
                    }
                }
            }
        }
    }

    fun filterSong(s: String) {
        if (s.isNotEmpty()) {
            getFilterSongFromApi(s)
        } else {
            getSongChartFromApi()
        }
    }

    fun showFavoriteSongList() {
        GlobalScope.launch(Dispatchers.IO) {
            val favSongList = SongDatabase.getDatabase(context!!).songDao().getAllSongFavorite()
            withContext(Dispatchers.Main){
                iFavSong?.onShowFavSongs(favSongList)
            }
        }

    }
}