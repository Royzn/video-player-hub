package com.example.video_player_hub.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

