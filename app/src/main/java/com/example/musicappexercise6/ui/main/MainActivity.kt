package com.example.musicappexercise6.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.adapter.SongListAdapter
import com.example.musicappexercise6.databinding.ActivityMainBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.Song
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.isPlaying
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.nowPlayingSong
import com.example.musicappexercise6.untils.Constants.CURRENT_SONG
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), ISong {

    lateinit var binding: ActivityMainBinding
    lateinit var presenter: SongPresenter


    companion object {
        lateinit var mSongList: MutableList<Song>
        const val TAG = "MainActivity"
       var adapter: SongListAdapter? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = SongPresenter(this, this)

        if (checkPermission())
            presenter.showSongList()

    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                123)
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
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    123)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(adapter != null)
            adapter?.notifyDataSetChanged()
    }

    override fun onShowSongList(songList: MutableList<Song>) {
        mSongList = songList
        adapter = SongListAdapter(this, songList)
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        adapter?.setIOnClickItemListener(object : IOnClickItem {
            override fun onClickItemListener(id: String) {
                var intent = Intent(this@MainActivity, MusicPlayerActivity::class.java)
                if (id == nowPlayingSong) {
                    intent.putExtra(EXTRA_TYPE, CURRENT_SONG)
                } else {
                    intent.putExtra(EXTRA_TYPE, TAG)
                }
                intent.putExtra(EXTRA_SONG_POSITION, id)
                startActivity(intent)
            }
        })
    }

    override fun onEmptySongList() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isPlaying && musicService!= null) {
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
}