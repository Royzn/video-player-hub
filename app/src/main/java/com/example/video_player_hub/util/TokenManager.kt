package com.example.video_player_hub.util

import android.content.Context

object TokenManager {
    private const val PREFS_NAME = "USER_TOKEN"
    private const val KEY_AUTH_TOKEN = ""

    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_AUTH_TOKEN, token)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(KEY_AUTH_TOKEN)
            apply()
        }
    }
}