package com.example.musicappexercise6.ui.detail.fragments

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.RelatedSongAdapter
import com.example.musicappexercise6.databinding.FragmentSongInfoBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.service.MusicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import com.example.musicappexercise6.ui.main.fragments.ChartFragment
import com.example.musicappexercise6.ui.main.fragments.MySongFragment
import com.example.musicappexercise6.untils.Constants

class SongInfoFragment : Fragment(), MediaPlayer.OnCompletionListener {

    companion object{
        fun newInstance(song: SongItem?): SongInfoFragment {
            val args = Bundle()
            args.putSerializable(Constants.BUNDLE_SONG, song)
            val fragment = SongInfoFragment()
            fragment.arguments = args
            return fragment
        }
        var song: SongItem? = null
        lateinit var binding: FragmentSongInfoBinding
        fun setSongUI(){
            binding.tvName.text = "Bài hát: ${song?.name}"
            binding.tvArtist.text = "Ca sĩ: ${song?.artists_names}"
            binding.tvAlbum.text = "Album: ${song?.name}"
        }
    }
    lateinit var adapter: RelatedSongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSongInfoBinding.inflate(inflater, container, false)
        song = arguments?.getSerializable(Constants.BUNDLE_SONG) as SongItem?
        setSongUI()
        setupRecyclerView()
        return binding.root
    }

    fun setupRecyclerView(){
        adapter = RelatedSongAdapter(requireContext(), songList)
        binding.rvSongRelated.adapter = adapter
        binding.rvSongRelated.layoutManager = LinearLayoutManager(requireContext())
        adapter.setIOnClickItemListener(object : IOnClickItem.ISongRelated{
            override fun onClickItemRelatedListener(song: SongItem) {

                MusicPlayerActivity.position = songList.indexOfFirst { it.id == song.id }
                createMusicPlayer()
                MusicPlayerActivity.setupTabLayout(parentFragmentManager, lifecycle)
            }

        })
    }

    fun createMusicPlayer() {
        try {
            if (MusicPlayerActivity.musicService!!.mediaPlayer == null)
                MusicPlayerActivity.musicService!!.mediaPlayer = MediaPlayer()

            MusicPlayerActivity.musicService?.mediaPlayer?.reset()
            if (!songList[MusicPlayerActivity.position].type.contains("storage"))
                MusicPlayerActivity.musicService?.mediaPlayer?.setDataSource(
                    "http://api.mp3.zing.vn/api/streaming/${songList[MusicPlayerActivity.position].type}/${songList[MusicPlayerActivity.position].id}/128"
                ) else
                MusicPlayerActivity.musicService?.mediaPlayer?.setDataSource(songList[MusicPlayerActivity.position].type)

            MusicPlayerActivity.musicService?.mediaPlayer?.prepare()
            MusicPlayerActivity.musicService?.mediaPlayer?.start()
            MusicPlayerActivity.isPlaying = true
            MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
            MusicPlayerActivity.binding.tvDurationPlayed.text =
                Constants.formattedTime(MusicPlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            MusicPlayerActivity.binding.tvDurationTotal.text =
                Constants.formattedTime(MusicPlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
            MusicPlayerActivity.binding.seekBar.progress = 0
            MusicPlayerActivity.binding.seekBar.max = MusicPlayerActivity.musicService!!.mediaPlayer!!.duration
            MusicPlayerActivity.musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            MusicPlayerActivity.nowPlayingSong = songList[MusicPlayerActivity.position].id
        } catch (e: Exception) {
            return
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (SongPresenter.isRepeatAll) {
            Constants.setSongPosition(true)
            createMusicPlayer()
            try {
                PlaySongFragment.setSongUI(requireContext())
                if (songList[MusicPlayerActivity.position].thumbnail != null)
                    Glide.with(this).load(songList[MusicPlayerActivity.position].thumbnail).into(
                        NowPlayingFragment.binding.ivSong
                    ) else
                    NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.skittle_chan)
                NowPlayingFragment.binding.tvTitle.text = ChartFragment.mSongList[MusicPlayerActivity.position].name
                NowPlayingFragment.binding.tvArtist.text = ChartFragment.mSongList[MusicPlayerActivity.position].artists_names
                NowPlayingFragment.binding.btnPlayAndPause.setImageResource(
                    if (MusicPlayerActivity.isPlaying)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play_arrow
                )
                MusicPlayerActivity.setupTabLayout(parentFragmentManager, lifecycle)
            } catch (e: Exception) {
                return
            }
        } else if (SongPresenter.isRepeatOne) {
            createMusicPlayer()
            try {
                PlaySongFragment.setSongUI(requireContext())
            } catch (e: Exception) {
                return
            }
        } else if (!SongPresenter.isRepeatAll && !SongPresenter.isRepeatOne) {
            MusicPlayerActivity.isPlaying = false
            MusicPlayerActivity.musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
            NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
            MusicPlayerActivity.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        }
    }

}