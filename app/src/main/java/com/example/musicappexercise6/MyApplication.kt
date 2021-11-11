package com.example.musicappexercise6

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.musicappexercise6.untils.Constants

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.CHANNEL_ID,
                "Now Playing Song",
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Important to showing song"
            channel.setSound(null, null)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
       // }
    }
}