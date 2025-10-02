package com.example.video_player_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.video_player_hub.util.TokenManager

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        val tvName = findViewById<TextView>(R.id.textName)
        val tvEmail = findViewById<TextView>(R.id.textEmail)
        val btnLogout = findViewById<Button>(R.id.buttonLogout)
        val btnBack = findViewById<Button>(R.id.buttonBack)


        val email = "user@example.com"

        // Hardcode name dari email
        val name = "Steven"

        // Set text
        tvName.text = "Name : $name"
        tvEmail.text = "Email : $email"

        btnLogout.setOnClickListener {
            // Clear token pakai TokenManager
            TokenManager.clearToken(this)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Kembali ke Login/MainActivity, clear backstack
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        btnBack.setOnClickListener {
            finish()
        }

    }
}
