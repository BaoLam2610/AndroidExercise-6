package com.example.musicappexercise6.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.TabLayoutAdapter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.isPlaying
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.position
//import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.setSongUI
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import com.example.musicappexercise6.ui.detail.fragments.NowPlayingFragment
import com.example.musicappexercise6.ui.detail.fragments.PlaySongFragment
import com.example.musicappexercise6.ui.detail.fragments.PlaySongFragment.Companion.setSongUI
import com.example.musicappexercise6.ui.detail.fragments.SongInfoFragment
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.untils.Constants.CLOSE
import com.example.musicappexercise6.untils.Constants.NEXT_SONG
import com.example.musicappexercise6.untils.Constants.PLAY_PAUSE_SONG
import com.example.musicappexercise6.untils.Constants.PREV_SONG
import com.example.musicappexercise6.untils.Constants.setSongPosition
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PLAY_PAUSE_SONG -> {
                if (isPlaying) pauseSong()
                else playSong()
            }
            NEXT_SONG -> prevNextSong(true, context!!)
            PREV_SONG -> prevNextSong(false, context!!)
            CLOSE -> {
                musicService!!.stopForeground(true)
                musicService!!.mediaPlayer!!.release()
                musicService = null
                exitProcess(1)
            }
        }


    }

    private fun playSong() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_pause, 1F)
        MusicPlayerActivity.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
    }

    private fun pauseSong() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
        MusicPlayerActivity.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment)
        musicService!!.createMusicPlayer()
        // set layout player activity
        setSongUI(context)
        playSong()
        // set fragment
        val song = songList[position]
        if (song.thumbnail != null)
            Glide.with(context).load(song.thumbnail).into(NowPlayingFragment.binding.ivSong)
        else
            NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.skittle_chan)
        NowPlayingFragment.binding.tvTitle.text = song.name
        NowPlayingFragment.binding.tvArtist.text = song.artists_names
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        playSong()
        MusicPlayerActivity.fragmentList = listOf(PlaySongFragment.newInstance(songList[position]), SongInfoFragment.newInstance(songList[position]))
//        MusicPlayerActivity.tabLayoutAdapter = TabLayoutAdapter(, lifecycle, MusicPlayerActivity.fragmentList!!)
//        MusicPlayerActivity.binding.viewPager2.adapter = MusicPlayerActivity.tabLayoutAdapter
//        MusicPlayerActivity.binding.indicator.setViewPager2(MusicPlayerActivity.binding.viewPager2)
        MusicPlayerActivity.binding.viewPager2.adapter?.notifyDataSetChanged()
        when {
            MainActivity.adapter != null -> MainActivity.adapter?.notifyDataSetChanged()
            MainActivity.filterAdapter != null -> MainActivity.filterAdapter?.notifyDataSetChanged()
        }
    }


}