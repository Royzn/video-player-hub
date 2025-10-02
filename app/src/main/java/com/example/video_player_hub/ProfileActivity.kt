package com.example.video_player_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.video_player_hub.util.TokenManager

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val emailFromIntent = intent.getStringExtra("email")

        // fallback email kalau tidak dikirim lewat intent
        val email = emailFromIntent ?: "user@example.com"

        setContent {
            ProfileScreen(
                email = email,
                onLogout = {
                    TokenManager.clearToken(this)
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

                    val i = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(i)
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(email: String, onLogout: () -> Unit) {
    val name = email.substringBefore("@")
        .replace('.', ' ')
        .split(' ')
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar statis dari drawable
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Avatar",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = name, style = MaterialTheme.typography.titleLarge)
        Text(text = email, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onLogout() }) {
            Text("Logout")
        }
    }
}
