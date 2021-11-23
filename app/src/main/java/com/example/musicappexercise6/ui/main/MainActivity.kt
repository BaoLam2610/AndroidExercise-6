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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.FilterSongAdapter
import com.example.musicappexercise6.adapter.SongListAdapter
import com.example.musicappexercise6.databinding.ActivityMainBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.network.RequestNetwork
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.isPlaying
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.nowPlayingSong
import com.example.musicappexercise6.ui.main.fragments.ChartFragment
import com.example.musicappexercise6.ui.main.fragments.FavoriteFragment
import com.example.musicappexercise6.ui.main.fragments.MySongFragment
import com.example.musicappexercise6.untils.Constants.CURRENT_SONG
import com.example.musicappexercise6.untils.Constants.EXTRA_SONG_POSITION
import com.example.musicappexercise6.untils.Constants.EXTRA_TYPE
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var requestNetwork: RequestNetwork
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        if (!checkNetwork())
        checkNetwork()
        setCurrentFragment(ChartFragment.newInstance())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mnChart -> {
                    checkNetwork()
                    setCurrentFragment(ChartFragment.newInstance())
                }
                R.id.mnFavorite -> setCurrentFragment(FavoriteFragment.newInstance())
                R.id.mnMySong -> setCurrentFragment(MySongFragment.newInstance())
            }
            true
        }

    }

    fun checkNetwork(): Boolean{
        requestNetwork = RequestNetwork(application)
        var check = false
        requestNetwork.observe(this){ isConnected ->
            val dialog = AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối mạng")
                .setMessage("Yêu cầu người dùng kết nối mạng")
                .setPositiveButton("Thoát") { _, _ ->
                    if (musicService != null) {
                        musicService!!.stopForeground(true)
                        musicService!!.mediaPlayer!!.release()
                        musicService = null
                        exitProcess(1)
                    }
                }
                .create()
            check = if(!isConnected){
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

    fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
                .commit()
        }
    }

    /*private fun checkPermission(): Boolean {
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
    }*/

    override fun onResume() {
        super.onResume()
//        when{
//            adapter != null -> adapter?.notifyDataSetChanged()
//            filterAdapter != null -> filterAdapter?.notifyDataSetChanged()
//        }
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

}