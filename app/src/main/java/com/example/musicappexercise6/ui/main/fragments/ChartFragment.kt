package com.example.musicappexercise6.ui.main.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.adapter.FilterSongAdapter
import com.example.musicappexercise6.adapter.SongListAdapter
import com.example.musicappexercise6.databinding.FragmentChartBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.model.chart.Song
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.untils.Constants
import kotlin.system.exitProcess

class ChartFragment : Fragment(), ISong, ISong.IFilterSong {

    companion object {
        fun newInstance(): ChartFragment {
            val args = Bundle()
            
            val fragment = ChartFragment()
            fragment.arguments = args
            return fragment
        }
        lateinit var binding: FragmentChartBinding
        lateinit var mSongList: MutableList<SongItem>
        const val TAG = "ChartFragment"
        var adapter: SongListAdapter? = null
        var filterAdapter: FilterSongAdapter? = null
    }

    lateinit var presenter: SongPresenter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)
        presenter = SongPresenter(requireContext(), this, this)

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
        return binding.root
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
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
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                //presenter.showSongList()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    123
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            ChartFragment.adapter != null -> ChartFragment.adapter?.notifyDataSetChanged()
            ChartFragment.filterAdapter != null -> ChartFragment.filterAdapter?.notifyDataSetChanged()
        }
    }

    override fun onShowSongList(songList: List<Song>) {
        binding.rvSongs.visibility = View.VISIBLE
        binding.rvFilterSongs.visibility = View.GONE
//        mSongList = songList as MutableList<Song>
        adapter = SongListAdapter(requireContext(), songList)
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        adapter?.setIOnClickItemListener(object : IOnClickItem.ISongChart {
            override fun onClickItemChartListener(song: Song) {
                var intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                if (song.id == MusicPlayerActivity.nowPlayingSong) {
                    intent.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
                } else {
                    intent.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                mSongList = presenter.getSongListRelated(song, null)
                intent.putExtra(Constants.EXTRA_SONG_POSITION, song.id)
                startActivity(intent)
            }

        })
    }


    override fun onEmptySongList() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!MusicPlayerActivity.isPlaying && MusicPlayerActivity.musicService != null) {
            MusicPlayerActivity.musicService?.stopForeground(true)
            MusicPlayerActivity.musicService!!.mediaPlayer!!.release()
            MusicPlayerActivity.musicService = null
            exitProcess(1)
        }
    }

    override fun onShowFilterSongs(filterSongList: List<FilterSong>) {
        binding.rvSongs.visibility = View.GONE
        binding.rvFilterSongs.visibility = View.VISIBLE
        filterAdapter = FilterSongAdapter(requireContext(), filterSongList)
        binding.rvFilterSongs.adapter = filterAdapter
        binding.rvFilterSongs.layoutManager = LinearLayoutManager(requireContext())
        filterAdapter?.setIOnClickItemListener(object : IOnClickItem.ISongFilter {
            override fun onClickItemFilterListener(filter: FilterSong) {
                var intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                if (filter.id == MusicPlayerActivity.nowPlayingSong) {
                    intent.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
                } else {
                    intent.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                mSongList = presenter.getSongListRelated(null, filter)
                intent.putExtra(Constants.EXTRA_SONG_POSITION, filter.id)
                startActivity(intent)
            }
        })
    }
}