package com.example.musicappexercise6.ui.detail.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicappexercise6.R
import com.example.musicappexercise6.databinding.FragmentNowPlayingBinding
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isShuffle
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.isPlaying
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.position
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import com.example.musicappexercise6.ui.main.MainActivity.Companion.adapter
import com.example.musicappexercise6.ui.main.MainActivity.Companion.mSongList
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import com.example.musicappexercise6.untils.Constants.setSongPosition

class NowPlayingFragment : Fragment() {

    companion object {
        lateinit var binding: FragmentNowPlayingBinding
        const val TAG = "NowPlayingFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.root.visibility = View.INVISIBLE
        binding.btnPlayAndPause.setOnClickListener {
            if (isPlaying) pauseSong()
            else playSong()
        }
        binding.btnNext.setOnClickListener {
            setSongPosition(true)
            musicService!!.createMusicPlayer()
            setSongBottomUI()
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playSong()
            if(adapter != null)
                adapter?.notifyDataSetChanged()
        }

        binding.btnPrevious.setOnClickListener {
            setSongPosition(false)
            musicService!!.createMusicPlayer()
            setSongBottomUI()
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playSong()
            if(adapter != null)
                adapter?.notifyDataSetChanged()
        }

        binding.root.setOnClickListener {
            Intent(requireContext(), MusicPlayerActivity::class.java).also {
                it.putExtra(EXTRA_SONG_POSITION, songList[position].id)
                it.putExtra(EXTRA_TYPE, TAG)
                startActivity(it)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.tvTitle.isSelected = true
            binding.tvArtist.isSelected = true
            setSongBottomUI()
        }
    }

    private fun setSongBottomUI() {
        if (songList[position].albumImage != null) {
            binding.ivSong.setImageBitmap(songList[position].albumImage)
        } else {
            binding.ivSong.setImageResource(R.drawable.unknown_song)
        }
        binding.tvTitle.text = songList[position].title
        binding.tvArtist.text = songList[position].artist

        binding.btnPlayAndPause.setImageResource(
            if (isPlaying)
                R.drawable.ic_pause
            else
                R.drawable.ic_play_arrow
        )
    }

    private fun playSong() {
        musicService!!.mediaPlayer!!.start()
        binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause, 1F)
        isPlaying = true
    }

    private fun pauseSong() {
        musicService!!.mediaPlayer!!.pause()
        binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
        isPlaying = false
    }
}