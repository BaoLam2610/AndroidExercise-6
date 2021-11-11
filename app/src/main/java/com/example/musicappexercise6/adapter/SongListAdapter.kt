package com.example.musicappexercise6.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicappexercise6.R
import com.example.musicappexercise6.databinding.ItemSongBinding
import com.example.musicappexercise6.event.IOnClickItem
import com.example.musicappexercise6.model.Song
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.main.MainActivity
import com.example.musicappexercise6.untils.Constants.formattedTime

class SongListAdapter(
    val mContext: Context,
    private val songList: MutableList<Song>
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>(){

    var iOnClickItem: IOnClickItem? = null

    fun setIOnClickItemListener(iOnClickItem: IOnClickItem){
        this.iOnClickItem = iOnClickItem
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(song: Song){
            with(binding){
                tvTitle.text = song.title
                tvArtist.text = song.artist
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvDurationTotal.text = formattedTime(song.duration)
                if(song.albumImage != null)
                    ivSong.setImageBitmap(song.albumImage)
                else
                    ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    iOnClickItem?.onClickItemListener(song.id)
                }
                if(MusicPlayerActivity.songList.isNotEmpty()) {
                    if (song.id == MusicPlayerActivity.songList[MusicPlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.BLACK)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if(MusicPlayerActivity.songList.isEmpty()){
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

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) = holder.bind(songList[position])

    override fun getItemCount(): Int = songList.size

}