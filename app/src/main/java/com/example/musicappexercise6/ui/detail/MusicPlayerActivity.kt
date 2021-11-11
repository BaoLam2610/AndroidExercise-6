package com.example.musicappexercise6.ui.detail

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.musicappexercise6.R
import com.example.musicappexercise6.databinding.ActivityMusicPlayerBinding
import com.example.musicappexercise6.model.Song
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.presenter.SongPresenter.Companion.editor
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isRepeatAll
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isRepeatOne
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isShuffle
import com.example.musicappexercise6.service.MusicService
import com.example.musicappexercise6.ui.detail.fragments.NowPlayingFragment
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.ui.main.MainActivity.Companion.mSongList
import com.example.musicappexercise6.untils.Constants.CURRENT_SONG
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ALL
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ONE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_SHUFFLE
import com.example.musicappexercise6.untils.Constants.formattedTime
import com.example.musicappexercise6.untils.Constants.setSongPosition
import java.util.*

class MusicPlayerActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener,
    ServiceConnection {

    companion object {

        private const val TAG = "MusicPlayerActivity"
        var musicService: MusicService? = null
        var position = -1
        var isPlaying = false
        var nowPlayingSong: String = ""
        var songList = mutableListOf<Song>()
        lateinit var binding: ActivityMusicPlayerBinding
        fun setSongUI() {
            val song = songList[position]
            binding.tvTitle.text = song.title.trim()
            binding.tvArtist.text = song.artist.trim()
            binding.tvTitle.isSelected = true
            binding.tvArtist.isSelected = true
            binding.tvDurationTotal.text = formattedTime(song.duration)
            if (song.albumImage != null) {
                binding.ivSong.setImageBitmap(song.albumImage)
                Palette.from(song.albumImage).generate {
                    val swatch = it?.dominantSwatch
                    var gradientSong: GradientDrawable
                    var gradientSong1: GradientDrawable
                    var gradientContainer: GradientDrawable
                    if (swatch != null) {
                        gradientSong = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(swatch.rgb, 0x00000000))
                        gradientSong1 = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(swatch.rgb, 0x00000000))
                        gradientContainer =
                            GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                intArrayOf(swatch.rgb, swatch.rgb))
                        binding.tvTitle.setTextColor(swatch.titleTextColor)
                        binding.tvArtist.setTextColor(swatch.titleTextColor)
                    } else {
                        gradientSong = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(0xff000000.toInt(), 0x00000000))
                        gradientSong1 = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(0xff000000.toInt(), 0x00000000))
                        gradientContainer =
                            GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                intArrayOf(0xff000000.toInt(), 0xff000000.toInt()))
                        binding.tvTitle.setTextColor(Color.WHITE)
                        binding.tvArtist.setTextColor(Color.WHITE)
                    }
                    binding.ivGradientImage.background = gradientSong
                    binding.ivGradientImage1.background = gradientSong1
                    binding.clContainer.background = gradientContainer
                }
            } else {
                binding.ivSong.setImageResource(R.drawable.unknown_song)
                binding.tvTitle.setTextColor(Color.WHITE)
                binding.tvArtist.setTextColor(Color.WHITE)
                binding.ivGradientImage.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player)
                binding.ivGradientImage1.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player_1)
                binding.clContainer.setBackgroundResource(R.drawable.custom_bgr_music_player)
            }
            if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) == true) {
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
            } else {
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            }
            if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_REPEAT_ONE, false) == true) {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
            } else if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_REPEAT_ALL,
                    false) == true
            ) {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            } else if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_REPEAT_ONE,
                    false) == false && SongPresenter.sharedPref?.getBoolean(SHARED_PREF_REPEAT_ALL,
                    false) == false
            ) {  // off repeat
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSongList()

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        // khi bật shuffle thì giữ nguyên vị trí nhạc đang phát và thay đổi các vị trí khác
        // khi tắt shuffle thì trả về vị trí hiện tại của nhạc đang phát tương ứng vs list ban đầu
        // và restore lại cái vị trí khác
        isShuffle = SongPresenter.sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) ?: false
        binding.btnShuffle.setOnClickListener {
            if (isShuffle) {    // shuffle on
                isShuffle = false
                editor?.putBoolean(SHARED_PREF_SHUFFLE, isShuffle)?.commit()
                var tempId = songList[position].id
                songList = mutableListOf()
                songList.addAll(mSongList)
                position = mSongList.indexOfFirst { it.id == tempId }
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            } else {            // off
                isShuffle = true
                editor?.putBoolean(SHARED_PREF_SHUFFLE, isShuffle)?.commit()
                songList = songList.shuffled() as MutableList<Song>
                Collections.swap(songList, position, songList.indexOfFirst { it.id == id })
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
            }
        }
        binding.btnRepeat.setOnClickListener {
            if (isRepeatOne) {
                isRepeatOne = false
                isRepeatAll = true
                editor?.putBoolean(SHARED_PREF_REPEAT_ONE, isRepeatOne)?.commit()
                editor?.putBoolean(SHARED_PREF_REPEAT_ALL, isRepeatAll)?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            } else if (isRepeatAll) {
                isRepeatAll = false
                editor?.putBoolean(SHARED_PREF_REPEAT_ALL, isRepeatAll)?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            } else if (!isRepeatOne && !isRepeatAll) {  // off repeat
                isRepeatOne = true
                editor?.putBoolean(SHARED_PREF_REPEAT_ONE, isRepeatOne)?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
            }
        }

        binding.btnBack.setOnClickListener {
            finish()

            //songList = mSongList
        }
    }

    var id: String? = ""
    private fun setSongList() {
        id = intent.getStringExtra(EXTRA_SONG_POSITION)
        songList = mSongList
        if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) == true) {
            songList = songList.shuffled() as MutableList<Song>
        }
        position = songList.indexOfFirst { it.id == id }
        when (intent.getStringExtra(EXTRA_TYPE)) {
            MainActivity.TAG -> {
                val it = Intent(this, MusicService::class.java)
                bindService(it, this, BIND_AUTO_CREATE)
                startService(it)
                setSongUI()
            }
            NowPlayingFragment.TAG, CURRENT_SONG -> {
                setSongUI()
                binding.tvDurationPlayed.text =
                    formattedTime(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvDurationPlayed.text =
                    formattedTime(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying)
                    binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
                else
                    binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
            }
        }
    }

    private fun initLayout() {
        createMusicPlayer()
        setSongUI()
    }

    private fun createMusicPlayer() {
        try {
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource(songList[position].path)
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()
            isPlaying = true
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            binding.tvDurationPlayed.text =
                formattedTime(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvDurationTotal.text =
                formattedTime(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingSong = songList[position].id
        } catch (e: Exception) {
            return
        }
    }


    override fun onResume() {
        super.onResume()
        playAndPauseSong()
        previousSong()
        nextSong()
    }

    private fun nextSong() {
        binding.btnNext.setOnClickListener {
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
            setSongPosition(true)
            initLayout()
        }
    }

    private fun previousSong() {
        binding.btnPrevious.setOnClickListener {
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
            setSongPosition(false)
            initLayout()
        }
    }


    private fun playAndPauseSong() {
        binding.btnPlayAndPause.setOnClickListener {
            if (isPlaying) {  // pause
                isPlaying = false
                musicService!!.showNotification(R.drawable.ic_play_arrow, 1F)
                binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
                musicService?.mediaPlayer?.pause()
            } else {
                isPlaying = true
                musicService!!.showNotification(R.drawable.ic_pause, 0F)
                binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
                musicService?.mediaPlayer?.start()
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (isRepeatAll) {
            setSongPosition(true)
            createMusicPlayer()
            try {
                setSongUI()
                if (mSongList[position].albumImage != null) {
                    NowPlayingFragment.binding.ivSong.setImageBitmap(mSongList[position].albumImage)
                } else {
                    NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.unknown_song)
                }
                NowPlayingFragment.binding.tvTitle.text = mSongList[position].title
                NowPlayingFragment.binding.tvArtist.text = mSongList[position].artist
                NowPlayingFragment.binding.btnPlayAndPause.setImageResource(
                    if (isPlaying)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play_arrow
                )
            } catch (e: Exception) {
                return
            }
        } else if (isRepeatOne) {
            createMusicPlayer()
            try {
                setSongUI()
            } catch (e: Exception) {
                return
            }
        } else if (!isRepeatAll && !isRepeatOne) {
            isPlaying = false
            musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
            NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMusicPlayer()
        musicService!!.setupSeekBarWithSong()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
}