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
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.untils.Constants.formattedTime

class FavoriteSongAdapter(
    val mContext: Context,
    private val favSongList: List<SongItem>
) : RecyclerView.Adapter<FavoriteSongAdapter.SongViewHolder>() {

    var iOnClickItem: IOnClickItem.ISongFavorite? = null

    fun setIOnClickItemListener(iOnClickItem: IOnClickItem.ISongFavorite) {
        this.iOnClickItem = iOnClickItem
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(favorite: SongItem) {
            with(binding) {
                tvTitle.text = favorite.name
                tvArtist.text = favorite.artists_names
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvTitle.setTextColor(Color.BLACK)
                tvArtist.setTextColor(Color.BLACK)
                tvDurationTotal.text = formattedTime(favorite.duration.toLong() * 1000)
                tvDurationTotal.setTextColor(Color.BLACK)
                if (favorite.thumbnail != null)
                    Glide.with(mContext).load(favorite.thumbnail).into(ivSong)
                else
                    binding.ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    iOnClickItem?.onClickItemFavoriteListener(favorite)
                }
                if (MusicPlayerActivity.songList.isNotEmpty()) {
                    if (favorite.id == MusicPlayerActivity.songList[MusicPlayerActivity.position].id) {
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
        holder.bind(favSongList[position])

    override fun getItemCount(): Int = favSongList.size

}