package com.example.video_player_hub.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchListDao {

    @Query("SELECT * FROM watch_list ORDER BY savedAt DESC")
    fun getAll(): Flow<List<WatchList>>  // Using Flow for live updates

    @Query("SELECT * FROM watch_list WHERE id = :postId")
    suspend fun getByPostId(postId: Int): WatchList?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(post: WatchList): Long

    @Query("DELETE FROM watch_list WHERE id = :watchlistId")
    suspend fun deleteById(watchlistId: Long)
}

