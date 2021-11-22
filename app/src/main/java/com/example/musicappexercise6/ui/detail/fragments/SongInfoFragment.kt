package com.example.musicappexercise6.ui.detail.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappexercise6.R
import com.example.musicappexercise6.adapter.RelatedSongAdapter
import com.example.musicappexercise6.databinding.FragmentSongInfoBinding
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import com.example.musicappexercise6.untils.Constants

class SongInfoFragment : Fragment() {

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
    }



}