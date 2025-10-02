package com.example.video_player_hub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.video_player_hub.network.ContentApiClient
import com.example.video_player_hub.util.TokenManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.launch

class ContentDetailActivity : ComponentActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private lateinit var bodyTextView: TextView
    private lateinit var backButton: Button

    private var exoPlayer: ExoPlayer? = null
    private lateinit var token: String
    private var postId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_detail_page)

        // Get views
        initViews()

        // Validate token
        token = TokenManager.getToken(this).orEmpty()
        if (token.isEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Get post ID
        postId = intent.getIntExtra("POST_ID", -1)
        if (postId == -1) {
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        backButton.setOnClickListener {
            finish()
        }

        // Fetch post and setup video
        getPostById(postId)
    }

    private fun initViews() {
        playerView = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        titleTextView = findViewById(R.id.titleTextView)
        bodyTextView = findViewById(R.id.bodyTextView)
        backButton = findViewById(R.id.backButton)
    }

    private fun getPostById(postId: Int) {
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch {
            try {
                val post = ContentApiClient.api.getPostById(token, postId)
                titleTextView.text = post.title
                bodyTextView.text = post.body
                playUri()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ContentDetailActivity, "Failed to load post: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun initPlayer(){
        if(exoPlayer == null){
            exoPlayer = ExoPlayer.Builder(this).build()
            playerView.player = exoPlayer
        }
    }

    private fun playUri(){
        initPlayer()

        val url = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4"
        val mediaItem = MediaItem.fromUri(Uri.parse(url))

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    progressBar.visibility =
                        if (state == Player.STATE_BUFFERING) ProgressBar.VISIBLE else ProgressBar.GONE
                }

                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(this@ContentDetailActivity, "Video error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart(); initPlayer()
    }

    override fun onStop() {
        super.onStop()
        playerView.player = null
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
