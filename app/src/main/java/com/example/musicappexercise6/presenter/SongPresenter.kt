package com.example.musicappexercise6.presenter

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.Song
import com.example.musicappexercise6.untils.Constants.MUSIC_SHARED_PREFERENCES
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ALL
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ONE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_SHUFFLE
import com.example.musicappexercise6.untils.Constants.getImageSongFromPath

class SongPresenter {

    var context: Context? = null
    var iSong: ISong? = null


    constructor(context: Context, iSong: ISong){
        this.context = context
        this.iSong = iSong
        sharedPref = context.getSharedPreferences(MUSIC_SHARED_PREFERENCES,Context.MODE_PRIVATE)
        editor = sharedPref?.edit()
    }
    companion object{
        var sharedPref: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null
        var isShuffle = sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) ?: false
        var isRepeatOne = sharedPref?.getBoolean(SHARED_PREF_REPEAT_ONE, false) ?: false
        var isRepeatAll = sharedPref?.getBoolean(SHARED_PREF_REPEAT_ALL, true) ?: true
    }

    fun showSongList(){
        var songList = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
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
                val song = Song(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    getImageSongFromPath(cursor.getString(3)),
                    cursor.getString(3),
                    cursor.getLong(4)
                )
                songList.add(song)
            }
        }
        if(songList.isNotEmpty())
            iSong!!.onShowSongList(songList)
        else
            iSong!!.onEmptySongList()
    }
}