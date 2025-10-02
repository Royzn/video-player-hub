package com.example.video_player_hub.network

import com.example.video_player_hub.data.LoginRequest
import com.example.video_player_hub.data.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ReqresApiService {
    @Headers(
        "x-api-key: reqres-free-v1",
        "Content-Type: application/json"
    )
    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

}