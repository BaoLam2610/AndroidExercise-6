package com.example.musicappexercise6.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.FavoriteSongAdapter
import com.example.musicappexercise6.databinding.FragmentFavoriteBinding
import com.example.musicappexercise6.databinding.FragmentMySongBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.untils.Constants

class MySongFragment : Fragment(), ISong.IMySong {

    companion object{
        const val TAG = "MySongFragment"
        lateinit var binding: FragmentMySongBinding
        fun newInstance(): MySongFragment {
            val args = Bundle()
            
            val fragment = MySongFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var presenter: SongPresenter
    var adapter: FavoriteSongAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMySongBinding.inflate(inflater, container, false)
        presenter = SongPresenter(requireContext(), this)
        presenter.showMySongList()
        return binding.root
    }

    override fun onShowMySongs(mySongList: List<SongItem>) {
        adapter = FavoriteSongAdapter(requireContext(), mySongList)
        binding.rvMySongs.adapter = adapter
        binding.rvMySongs.layoutManager = LinearLayoutManager(requireContext())
        adapter?.setIOnClickItemListener(object : IOnClickItem.ISongFavorite{
            override fun onClickItemFavoriteListener(song: SongItem) {
                var intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                if (song.id == MusicPlayerActivity.nowPlayingSong) {
                    intent.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
                } else {
                    intent.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                ChartFragment.mSongList = mySongList as MutableList<SongItem>
                intent.putExtra(Constants.EXTRA_SONG_POSITION, song.id)
                startActivity(intent)
            }

        })
    }

}