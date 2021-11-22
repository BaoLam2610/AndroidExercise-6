package com.example.musicappexercise6.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.databinding.ItemSongBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.model.filter.FilterSong

import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.untils.Constants.formattedTime

class FilterSongAdapter(
    val mContext: Context,
    private val filterSongList: List<FilterSong>
) : RecyclerView.Adapter<FilterSongAdapter.SongViewHolder>() {

    var iOnClickItem: IOnClickItem.ISongFilter? = null

    fun setIOnClickItemListener(iOnClickItem: IOnClickItem.ISongFilter) {
        this.iOnClickItem = iOnClickItem
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(filterSong: FilterSong) {
            with(binding) {
                tvTitle.text = filterSong.name
                tvArtist.text = filterSong.artist
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvDurationTotal.text = formattedTime(filterSong.duration.toLong() * 1000)
                if (filterSong.thumb != null)
                    Glide.with(mContext)
                        .load("https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${filterSong.thumb}")
                        .into(ivSong)
                else
                    ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    iOnClickItem?.onClickItemFilterListener(filterSong)
                }
                if (MusicPlayerActivity.songList.isNotEmpty()) {
                    if (filterSong.id == MusicPlayerActivity.songList[MusicPlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.BLACK)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if (MusicPlayerActivity.songList.isEmpty()) {
                    tvTitle.setTextColor(Color.BLACK)
//                    tvArtist.setTextColor(R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) =
        holder.bind(filterSongList[position])

    override fun getItemCount(): Int = filterSongList.size

}