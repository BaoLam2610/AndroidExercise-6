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
import com.example.musicappexercise6.model.filter.FilterSong
import com.example.musicappexercise6.model.related.RelatedSong

import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.untils.Constants.formattedTime

class RelatedSongAdapter(
    val mContext: Context,
    private val relatedSongList: List<SongItem>
) : RecyclerView.Adapter<RelatedSongAdapter.SongViewHolder>(){

    var iOnClickItem: IOnClickItem.ISongRelated? = null

    fun setIOnClickItemListener(iOnClickItem: IOnClickItem.ISongRelated){
        this.iOnClickItem = iOnClickItem
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(related: SongItem){
            with(binding){
                tvTitle.text = related.name
                tvArtist.text = related.artists_names
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvTitle.setTextColor(Color.WHITE)
                tvArtist.setTextColor(Color.WHITE)
                tvDurationTotal.text = formattedTime(related.duration.toLong()* 1000)
                tvDurationTotal.setTextColor(Color.WHITE)
                if(related.thumbnail != null)
                    Glide.with(mContext).load(related.thumbnail).into(ivSong)
                else
                    binding.ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    iOnClickItem?.onClickItemRelatedListener(related)
                }
                if(MusicPlayerActivity.songList.isNotEmpty()) {
                    if (related.id == MusicPlayerActivity.songList[MusicPlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.WHITE)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if(MusicPlayerActivity.songList.isEmpty()){
                    tvTitle.setTextColor(Color.WHITE)
//                    tvArtist.setTextColor(R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) = holder.bind(relatedSongList[position])

    override fun getItemCount(): Int = relatedSongList.size

}