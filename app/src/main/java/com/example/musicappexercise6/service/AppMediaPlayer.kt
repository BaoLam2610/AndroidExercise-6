package com.example.musicappexercise6.service

import android.media.MediaPlayer

class AppMediaPlayer {
    companion object {
        var _instance : MediaPlayer? = null

        fun getInstance() : MediaPlayer{
            if (_instance == null) {
                _instance = MediaPlayer()
            }
            return _instance as MediaPlayer
        }

        val currentIndex = -1
    }
}