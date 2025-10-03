package com.example.video_player_hub.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_list")
data class WatchList(
    @PrimaryKey(autoGenerate = false) val id: Long = 0,
    val title: String,
    val excerpt: String,
    val savedAt: Long = System.currentTimeMillis()
)