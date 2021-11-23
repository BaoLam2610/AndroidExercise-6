package com.example.musicappexercise6.ui.detail

import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.TabLayoutAdapter
import com.example.musicappexercise6.databinding.ActivityMusicPlayerBinding
import com.example.musicappexercise6.db.SongDatabase
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.network.RequestNetwork
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.presenter.SongPresenter.Companion.editor
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isRepeatAll
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isRepeatOne
import com.example.musicappexercise6.presenter.SongPresenter.Companion.isShuffle
import com.example.musicappexercise6.service.MusicService
import com.example.musicappexercise6.ui.detail.fragments.NowPlayingFragment
import com.example.musicappexercise6.ui.detail.fragments.PlaySongFragment
import com.example.musicappexercise6.ui.detail.fragments.PlaySongFragment.Companion.setSongUI
import com.example.musicappexercise6.ui.detail.fragments.SongInfoFragment
import com.example.musicappexercise6.ui.main.fragments.ChartFragment
import com.example.musicappexercise6.ui.main.fragments.ChartFragment.Companion.mSongList
import com.example.musicappexercise6.ui.main.fragments.FavoriteFragment
import com.example.musicappexercise6.ui.main.fragments.MySongFragment
import com.example.musicappexercise6.untils.Constants.CURRENT_SONG
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ALL
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_REPEAT_ONE
import com.example.musicappexercise6.untils.Constants.SHARED_PREF_SHUFFLE
import com.example.musicappexercise6.untils.Constants.formattedTime
import com.example.musicappexercise6.untils.Constants.setSongPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.system.exitProcess

class MusicPlayerActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener,
    ServiceConnection {

    companion object {

        private const val TAG = "MusicPlayerActivity"
        var musicService: MusicService? = null
        var position = -1
        var isPlaying = false
        var nowPlayingSong: String = ""
        var songList = mutableListOf<SongItem>()
        lateinit var binding: ActivityMusicPlayerBinding
        var tabLayoutAdapter: TabLayoutAdapter? = null
        var fragmentList: List<Fragment>? = null
        fun setupTabLayout(fragmentManager: FragmentManager, lifecycle: Lifecycle) {
            fragmentList = listOf(
                PlaySongFragment.newInstance(songList[position]),
                SongInfoFragment.newInstance(songList[position])
            )
            tabLayoutAdapter = TabLayoutAdapter(fragmentManager, lifecycle, fragmentList!!)
            binding.viewPager2.adapter = tabLayoutAdapter
            binding.indicator.setViewPager2(binding.viewPager2)
        }

        fun checkFavorite(context: Context): Boolean {
            val favoriteList = SongDatabase.getDatabase(context).songDao()
                .getAllSongFavorite()
            for (item in favoriteList) {
                if (songList[position].id == item.id)
                    return true
            }
            return false
        }
    }

    lateinit var requestNetwork: RequestNetwork
    fun checkNetwork(): Boolean {
        requestNetwork = RequestNetwork(application)
        var check = false

        requestNetwork.observe(this) { isConnected ->
            val dialog = AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối mạng")
                .setMessage("Yêu cầu người dùng kết nối mạng")
                .setPositiveButton("Thoát") { _, _ ->
                    exitProcess(1)
                }
                .create()
            check = if (!isConnected) {
                dialog.show()
                dialog.setCanceledOnTouchOutside(true)
                false
            } else {
                dialog.setCanceledOnTouchOutside(false)
                dialog.dismiss()
                true
            }
        }
        return check
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //val playSongFragment = PlaySongFragment.newInstance(null)
        requestNetwork = RequestNetwork(application)
        checkNetwork()
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
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
                songList = songList.shuffled() as MutableList<SongItem>
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

        binding.btnDownload.setOnClickListener {
            val url =
                "http://api.mp3.zing.vn/api/streaming/${songList[position].type}/${songList[position].id}/128"
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(songList[position].name)
            request.setDescription("Download song...")
            request.setNotificationVisibility((DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MUSIC, System.currentTimeMillis().toString()+".mp3")
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            try {
                manager?.enqueue(request)
                Toast.makeText(this, "Tải thành công", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
        setSongList()
        setupTabLayout(supportFragmentManager, lifecycle)
        binding.btnFavorite.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (!checkFavorite(this@MusicPlayerActivity)) {
                    SongDatabase.getDatabase(this@MusicPlayerActivity).songDao()
                        .addSong(songList[position])
                    withContext(Dispatchers.Main) {
                        binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                        Toast.makeText(
                            this@MusicPlayerActivity,
                            "Đã thêm bài hát vào danh sách yêu thích",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    SongDatabase.getDatabase(this@MusicPlayerActivity).songDao()
                        .deleteSong(songList[position])
                    withContext(Dispatchers.Main) {
                        binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                        Toast.makeText(
                            this@MusicPlayerActivity,
                            "Đã xóa bài hát khỏi danh sách yêu thích",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }


    var id: String? = ""
    private fun setSongList() {
        id = intent.getStringExtra(EXTRA_SONG_POSITION)
        songList = mSongList
        if (SongPresenter.sharedPref?.getBoolean(SHARED_PREF_SHUFFLE, false) == true) {
            songList = songList.shuffled() as MutableList<SongItem>
        }
        position = songList.indexOfFirst { it.id == id }

        when (intent.getStringExtra(EXTRA_TYPE)) {
            ChartFragment.TAG, FavoriteFragment.TAG, MySongFragment.TAG -> {
                val it = Intent(this, MusicService::class.java)
                bindService(it, this, BIND_AUTO_CREATE)
                startService(it)
                setSongUI(this)
            }
            NowPlayingFragment.TAG, CURRENT_SONG -> {
                setSongUI(applicationContext)
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
        setSongUI(applicationContext)
    }

    fun createMusicPlayer() {
        try {
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()

            musicService?.mediaPlayer?.reset()
            if (intent.getStringExtra(EXTRA_TYPE) != MySongFragment.TAG)
                musicService?.mediaPlayer?.setDataSource(
                    "http://api.mp3.zing.vn/api/streaming/${songList[position].type}/${songList[position].id}/128"
                ) else
                musicService?.mediaPlayer?.setDataSource(songList[position].type)

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
            setupTabLayout(supportFragmentManager, lifecycle)
        }
    }

    private fun previousSong() {
        binding.btnPrevious.setOnClickListener {
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
            setSongPosition(false)
            initLayout()
            setupTabLayout(supportFragmentManager, lifecycle)
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
                setSongUI(this)
                if (songList[position].thumbnail != null)
                    Glide.with(this).load(songList[position].thumbnail).into(
                        NowPlayingFragment.binding.ivSong
                    ) else
                    NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.skittle_chan)
                NowPlayingFragment.binding.tvTitle.text = mSongList[position].name
                NowPlayingFragment.binding.tvArtist.text = mSongList[position].artists_names
                NowPlayingFragment.binding.btnPlayAndPause.setImageResource(
                    if (isPlaying)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play_arrow
                )
                setupTabLayout(supportFragmentManager, lifecycle)
            } catch (e: Exception) {
                return
            }
        } else if (isRepeatOne) {
            createMusicPlayer()
            try {
                setSongUI(this)
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