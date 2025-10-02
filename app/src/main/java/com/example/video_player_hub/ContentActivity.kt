package com.example.video_player_hub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.video_player_hub.adapter.ContentAdapter
import com.example.video_player_hub.data.Post
import com.example.video_player_hub.network.ContentApiClient
import com.example.video_player_hub.util.TokenManager
import kotlinx.coroutines.launch

class ContentActivity : ComponentActivity() {

    private lateinit var contentRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var contentAdapter: ContentAdapter
    private lateinit var token: String
    private var fullPostList = listOf<Post>()
    private lateinit var progressBar: ProgressBar
    private lateinit var profileButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_page)

        token = TokenManager.getToken(this).toString()
        if (token.isEmpty()) {
            // Redirect to LoginActivity and clear back stack so user can't go back here
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        initView()

        contentAdapter = ContentAdapter(
            fullList = mutableListOf(),
            onViewDetailClick = { post ->
                onViewDetailClick(post)
            }
        )
        contentRecyclerView.layoutManager = LinearLayoutManager(this)
        contentRecyclerView.adapter = contentAdapter

        loadPosts()

        setupSearch()

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun initView(){
        contentRecyclerView = findViewById(R.id.contentRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        progressBar = findViewById(R.id.progressBar)
        profileButton = findViewById(R.id.profileButton)
        errorTextView = findViewById(R.id.errorTextView)
        emptyTextView = findViewById(R.id.emptyTextView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
    }

    private fun loadPosts() {
        progressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        emptyTextView.visibility = View.GONE
        contentRecyclerView.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

        lifecycleScope.launch {
            try {
                fullPostList = ContentApiClient.api.getPosts(token)
                if (fullPostList.isEmpty()) {
                    // No data found
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    contentAdapter.setData(fullPostList)
                    contentRecyclerView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorTextView.text = "Failed to load posts. Please check your connection."
                errorTextView.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterPosts(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterPosts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            fullPostList
        } else {
            fullPostList.filter { post ->
                post.title.contains(query, ignoreCase = true)
            }
        }

        contentAdapter.setData(filteredList)

        if (filteredList.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            contentRecyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            contentRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun onViewDetailClick(post: Post) {
        val intent = Intent(this, ContentDetailActivity::class.java).apply {
            putExtra("POST_ID", post.id)
        }
        startActivity(intent)
    }
}
