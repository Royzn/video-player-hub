package com.example.video_player_hub.network

import com.example.video_player_hub.data.Post
import retrofit2.http.Headers
import com.example.video_player_hub.util.TokenManager
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ContentApiService {
    @GET("posts")
    suspend fun getPosts(@Header("Authorization") authHeader: String): List<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Header("Authorization") authHeader: String, @Path("id") id: Int): Post
}