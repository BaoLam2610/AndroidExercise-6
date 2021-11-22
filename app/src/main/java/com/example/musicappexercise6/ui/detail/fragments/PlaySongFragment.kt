package com.example.musicappexercise6.ui.detail.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.databinding.FragmentPlaySongBinding
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.presenter.SongPresenter
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.checkFavorite
import com.example.musicappexercise6.untils.Constants
import com.example.musicappexercise6.untils.Constants.BUNDLE_SONG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaySongFragment : Fragment() {


    companion object {
        fun newInstance(song: SongItem?): PlaySongFragment {
            val args = Bundle()
            args.putSerializable(BUNDLE_SONG, song)
            val fragment = PlaySongFragment()
            fragment.arguments = args
            return fragment
        }

        var song: SongItem? = null
        var binding: FragmentPlaySongBinding? = null

        fun setSongUI(context: Context) {

            binding?.tvTitle?.text = song!!.name.trim()
            binding?.tvArtist?.text = song!!.artists_names.trim()
            binding?.tvTitle?.isSelected = true
            binding?.tvArtist?.isSelected = true
            val song = MusicPlayerActivity.songList[MusicPlayerActivity.position]
            var bitmap: Bitmap? = null
            MusicPlayerActivity.binding.tvDurationTotal.text =
                Constants.formattedTime(song.duration.toLong() * 1000)
            GlobalScope.launch(Dispatchers.Default) {
                bitmap = if (song.thumbnail != null)
                    Glide.with(context)
                        .asBitmap()
                        .load(song.thumbnail)
                        .submit()
                        .get()
                else null
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding?.ivSong?.setImageBitmap(bitmap)
                        Palette.from(bitmap!!).generate {
                            val swatch = it?.dominantSwatch
                            var gradientSong: GradientDrawable
                            var gradientSong1: GradientDrawable
                            var gradientContainer: GradientDrawable
                            if (swatch != null) {
                                gradientSong =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.BOTTOM_TOP,
                                        intArrayOf(swatch.rgb, 0x00000000)
                                    )
                                gradientSong1 =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(swatch.rgb, 0x00000000)
                                    )
                                gradientContainer =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.BOTTOM_TOP,
                                        intArrayOf(swatch.rgb, swatch.rgb)
                                    )
                                binding?.tvTitle?.setTextColor(swatch.titleTextColor)
                                binding?.tvArtist?.setTextColor(swatch.titleTextColor)
                            } else {
                                gradientSong =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.BOTTOM_TOP,
                                        intArrayOf(0xff000000.toInt(), 0x00000000)
                                    )
                                gradientSong1 =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(0xff000000.toInt(), 0x00000000)
                                    )
                                gradientContainer =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.BOTTOM_TOP,
                                        intArrayOf(0xff000000.toInt(), 0xff000000.toInt())
                                    )
                                binding?.tvTitle?.setTextColor(Color.WHITE)
                                binding?.tvArtist?.setTextColor(Color.WHITE)
                            }
                            binding?.ivGradientImage?.background = gradientSong
                            binding?.ivGradientImage1?.background = gradientSong1
                            MusicPlayerActivity.binding.clContainer.background = gradientContainer
                        }
                    } else {
                        binding?.ivSong?.setImageResource(R.drawable.unknown_song)
                        binding?.tvTitle?.setTextColor(Color.WHITE)
                        binding?.tvArtist?.setTextColor(Color.WHITE)
                        binding?.ivGradientImage?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player)
                        binding?.ivGradientImage1?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player_1)
                        MusicPlayerActivity.binding.clContainer.setBackgroundResource(R.drawable.custom_bgr_music_player)
                    }
                    if (SongPresenter.sharedPref?.getBoolean(
                            Constants.SHARED_PREF_SHUFFLE,
                            false
                        ) == true
                    ) {
                        MusicPlayerActivity.binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
                    } else {
                        MusicPlayerActivity.binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
                    }
                    if (SongPresenter.sharedPref?.getBoolean(
                            Constants.SHARED_PREF_REPEAT_ONE,
                            false
                        ) == true
                    ) {
                        MusicPlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                    } else if (SongPresenter.sharedPref?.getBoolean(
                            Constants.SHARED_PREF_REPEAT_ALL,
                            false
                        ) == true
                    ) {
                        MusicPlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                    } else if (SongPresenter.sharedPref?.getBoolean(
                            Constants.SHARED_PREF_REPEAT_ONE,
                            false
                        ) == false && SongPresenter.sharedPref?.getBoolean(
                            Constants.SHARED_PREF_REPEAT_ALL,
                            false
                        ) == false
                    ) {  // off repeat
                        MusicPlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
                    }
                }
            }
            GlobalScope.launch(Dispatchers.IO) {
                if (checkFavorite(context)) {
                    withContext(Dispatchers.Main) {
                        MusicPlayerActivity.binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        MusicPlayerActivity.binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                    }
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlaySongBinding.inflate(inflater, container, false)
        song = arguments?.getSerializable(BUNDLE_SONG) as SongItem?
        if (song != null)
            setSongUI(requireContext())
        return binding?.root
    }


}