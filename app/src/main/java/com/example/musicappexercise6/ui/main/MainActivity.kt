package com.example.musicappexercise6.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.adapter.FilterSongAdapter
import com.example.musicappexercise6.adapter.SongListAdapter
import com.example.musicappexercise6.databinding.ActivityMainBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.isPlaying
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.nowPlayingSong
import com.example.musicappexercise6.untils.Constants.CURRENT_SONG
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), ISong, ISong.IFilterSong {

    lateinit var binding: ActivityMainBinding
    lateinit var presenter: SongPresenter


    companion object {
        lateinit var mSongList: MutableList<SongItem>
        const val TAG = "MainActivity"
        var adapter: SongListAdapter? = null
        var filterAdapter: FilterSongAdapter? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = SongPresenter(this, this, this)

        if (checkPermission()) {
//            presenter.showSongList()
//            presenter.getSongChartFromApi()
        }
        presenter.getSongChartFromApi()
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                presenter.filterSong(s.toString())
            }

        })
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                123
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                presenter.showSongList()

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    123
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when{
            adapter != null -> adapter?.notifyDataSetChanged()
            filterAdapter != null -> filterAdapter?.notifyDataSetChanged()
        }
    }

    override fun onShowSongList(songList: List<Song>) {
        binding.rvSongs.visibility = View.VISIBLE
        binding.rvFilterSongs.visibility = View.GONE
//        mSongList = songList as MutableList<Song>
        adapter = SongListAdapter(this, songList)
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        adapter?.setIOnClickItemListener(object : IOnClickItem.ISongChart {
            override fun onClickItemChartListener(song: Song) {
                var intent = Intent(this@MainActivity, MusicPlayerActivity::class.java)
                if (song.id == nowPlayingSong) {
                    intent.putExtra(EXTRA_TYPE, CURRENT_SONG)
                } else {
                    intent.putExtra(EXTRA_TYPE, TAG)
                }
                mSongList = presenter.getSongListRelated(song,null)
                intent.putExtra(EXTRA_SONG_POSITION, song.id)
                startActivity(intent)
            }

        })
    }


    override fun onEmptySongList() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isPlaying && musicService != null) {
            musicService?.stopForeground(true)
            musicService!!.mediaPlayer!!.release()
            musicService = null
            exitProcess(1)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val dialog = AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                if (musicService != null) {
                    musicService!!.stopForeground(true)
                    musicService!!.mediaPlayer!!.release()
                    musicService = null
                    exitProcess(1)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    override fun onShowFilterSongs(filterSongList: List<FilterSong>) {
        binding.rvSongs.visibility = View.GONE
        binding.rvFilterSongs.visibility = View.VISIBLE
        filterAdapter = FilterSongAdapter(this, filterSongList)
        binding.rvFilterSongs.adapter = filterAdapter
        binding.rvFilterSongs.layoutManager = LinearLayoutManager(this)
        filterAdapter?.setIOnClickItemListener(object : IOnClickItem.ISongFilter {
            override fun onClickItemFilterListener(filter: FilterSong) {
                var intent = Intent(this@MainActivity, MusicPlayerActivity::class.java)
                if (filter.id == nowPlayingSong) {
                    intent.putExtra(EXTRA_TYPE, CURRENT_SONG)
                } else {
                    intent.putExtra(EXTRA_TYPE, TAG)
                }
                mSongList = presenter.getSongListRelated(null,filter)
                intent.putExtra(EXTRA_SONG_POSITION, filter.id)
                startActivity(intent)
            }
        })
    }
}