package com.example.video_player_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.video_player_hub.data.LoginRequest
import com.example.video_player_hub.data.LoginResponse
import com.example.video_player_hub.network.ReqresApiClient
import com.example.video_player_hub.util.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = TokenManager.getToken(this)
        if (!token.isNullOrEmpty()) {
            // Token exists, navigate directly to ContentActivity
            val intent = Intent(this, ContentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Close login activity
            return // Prevent rest of onCreate from running
        }

        setContentView(R.layout.login_page)

        initViews()

        loginButton.setOnClickListener {
            login()
        }
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        progress = findViewById(R.id.progress) // You missed this
    }

    private fun login() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Show ProgressBar, hide button
        progress.visibility = View.VISIBLE
        loginButton.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val token = ReqresApiClient.api.loginUser(LoginRequest(email, password))
                TokenManager.saveToken(this@MainActivity, token.token)

                val intent = Intent(this@MainActivity, ContentActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                progress.visibility = View.GONE
                loginButton.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                loginButton.visibility = View.VISIBLE
            }
        }
    }
}
