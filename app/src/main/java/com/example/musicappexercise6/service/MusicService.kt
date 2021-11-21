package com.example.musicappexercise6.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.musicappexercise6.R
import com.example.musicappexercise6.receiver.NotificationReceiver
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.musicService
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.position
import com.example.musicappexercise6.ui.detail.MusicPlayerActivity.Companion.songList
import com.example.musicappexercise6.untils.Constants
import com.example.musicappexercise6.untils.Constants.CHANNEL_ID
import com.example.musicappexercise6.untils.Constants.CLOSE
import com.example.musicappexercise6.untils.Constants.NEXT_SONG
import com.example.musicappexercise6.untils.Constants.PLAY_PAUSE_SONG
import com.example.musicappexercise6.untils.Constants.PREV_SONG
import kotlinx.coroutines.*
import java.lang.Runnable

class MusicService : Service() {
    private val TAG = "MusicService"
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(btnPlayPause: Int, playBackSpeed: Float) {
        // handle click notif
        val intent = Intent(baseContext, MusicPlayerActivity::class.java)
        intent.putExtra(Constants.EXTRA_SONG_POSITION, songList[position].id)
        intent.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // prev
        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(PREV_SONG)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // play
        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            PLAY_PAUSE_SONG
        )
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // next
        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(NEXT_SONG)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // close
        val closeIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(CLOSE)
        val closePendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        val song = songList[position]
        GlobalScope.launch(Dispatchers.Default) {
            var bitmap = if (song.thumbnail != null) Glide.with(applicationContext).asBitmap()
                .load(song.thumbnail).submit().get()
            else
                BitmapFactory.decodeResource(resources, R.drawable.skittle_chan)
            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(baseContext, CHANNEL_ID)
                    .setContentTitle(song.name)
                    .setContentText(song.artists_names)
                    .setSmallIcon(R.drawable.ic_music)
                    .setLargeIcon(bitmap)
//            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
//                .setMediaSession(mediaSession.sessionToken))//androidx.media.app.NotificationCompat
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                    .addAction(btnPlayPause, "Play", playPendingIntent)
                    .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                    .addAction(R.drawable.ic_close, "Exit", closePendingIntent)
                    .setContentIntent(pendingIntent)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaSession.setMetadata(
                        MediaMetadataCompat.Builder()
                            .putLong(
                                MediaMetadataCompat.METADATA_KEY_DURATION,
                                mediaPlayer!!.duration.toLong()
                            )
                            .build()
                    )
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(
                                PlaybackStateCompat.STATE_PLAYING,
                                mediaPlayer!!.currentPosition.toLong(),
                                playBackSpeed
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .build()
                    )
                }

                startForeground(13, notification)
            }
        }


    }

    fun createMusicPlayer() {
        try {
            val song = songList[position]
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource("http://api.mp3.zing.vn/api/streaming/${song.type}/${song.id}/128")
            musicService?.mediaPlayer?.prepare()
            musicService!!.showNotification(R.drawable.ic_pause, 0F)

            MusicPlayerActivity.binding.tvDurationPlayed.text =
                Constants.formattedTime(mediaPlayer!!.currentPosition.toLong())
            MusicPlayerActivity.binding.tvDurationTotal.text =
                Constants.formattedTime(mediaPlayer!!.duration.toLong())

            MusicPlayerActivity.binding.seekBar.progress = 0
            MusicPlayerActivity.binding.seekBar.max = mediaPlayer!!.duration
            MusicPlayerActivity.nowPlayingSong = songList[position].id
        } catch (e: Exception) {
            return
        }
    }

    fun setupSeekBarWithSong() {
        runnable = Runnable {
            MusicPlayerActivity.binding.tvDurationPlayed.text =
                Constants.formattedTime(mediaPlayer!!.currentPosition.toLong())
            MusicPlayerActivity.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
    }
}