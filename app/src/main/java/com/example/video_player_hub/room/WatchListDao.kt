package com.example.video_player_hub.room

import androidx.room.*

@Dao
interface WatchListDao {

    @Query("SELECT * FROM watch_list ORDER BY savedAt DESC")
    suspend fun getAll(): List<WatchList>

    @Query("SELECT * FROM watch_list WHERE id = :postId")
    suspend fun getByPostId(postId: Int): WatchList?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(post: WatchList): Long

    @Delete
    suspend fun delete(post: WatchList)
}
