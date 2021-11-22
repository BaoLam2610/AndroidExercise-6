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
import com.example.musicappexercise6.adapter.RelatedSongAdapter
import com.example.musicappexercise6.databinding.FragmentFavoriteBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.event.ISong
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.untils.Constants

class FavoriteFragment : Fragment(), ISong.IFavoriteSong {

    companion object{
        const val TAG = "FavoriteFragment"
        lateinit var binding: FragmentFavoriteBinding
        fun newInstance(): FavoriteFragment {
            val args = Bundle()
            
            val fragment = FavoriteFragment()
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
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        presenter = SongPresenter(requireContext(), this)
        presenter.showFavoriteSongList()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    override fun onShowFavSongs(favSongList: List<SongItem>) {
        adapter = FavoriteSongAdapter(requireContext(), favSongList)
        binding.rvFavoriteSongs.adapter = adapter
        binding.rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext())
        adapter?.setIOnClickItemListener(object : IOnClickItem.ISongFavorite{
            override fun onClickItemFavoriteListener(song: SongItem) {
                var intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                if (song.id == MusicPlayerActivity.nowPlayingSong) {
                    intent.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
                } else {
                    intent.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                ChartFragment.mSongList = favSongList as MutableList<SongItem>
                intent.putExtra(Constants.EXTRA_SONG_POSITION, song.id)
                startActivity(intent)
            }

        })
    }

}