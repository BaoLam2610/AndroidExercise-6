package com.example.musicappexercise6.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicappexercise6.model.SongItem
import com.example.musicappexercise6.untils.Constants.DB_NAME

@Database(
    entities = [SongItem::class],
    version = 1,
    exportSchema = false
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var instance: SongDatabase? = null

        fun getDatabase(context: Context): SongDatabase = instance ?: synchronized(this) {
            return Room.databaseBuilder(
                context.applicationContext,
                SongDatabase::class.java,
                DB_NAME
            ).build().also {
                instance = it
            }
        }

    }
}