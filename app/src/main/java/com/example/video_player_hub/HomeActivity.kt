package com.example.video_player_hub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.video_player_hub.ui.ContentFragment

class HomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ContentFragment())
                .commit()
        }
    }
}