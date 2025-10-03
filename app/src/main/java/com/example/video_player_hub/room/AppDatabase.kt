package com.example.video_player_hub.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WatchList::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchListDao(): WatchListDao

    companion object {
        @Volatile private var INSTANCE: com.example.video_player_hub.room.AppDatabase? = null

        fun get(ctx: Context): com.example.video_player_hub.room.AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    com.example.video_player_hub.room.AppDatabase::class.java,
                    "app-db"
                )
                    .allowMainThreadQueries()
                    .build().also { INSTANCE = it }
            }
    }
}
