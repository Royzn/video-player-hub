package com.example.video_player_hub.vm

import android.util.Log
import androidx.lifecycle.*
import com.example.video_player_hub.data.Post
import com.example.video_player_hub.network.ContentApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ContentUiState {
    object Idle : ContentUiState()
    object Loading : ContentUiState()
    data class Success(val posts: List<Post>) : ContentUiState()
    data class Error(val message: String) : ContentUiState()
}

class ContentViewModel(private val token: String) : ViewModel() {

    private val _uiState = MutableStateFlow<ContentUiState>(ContentUiState.Idle)
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    private val _fullPostList = mutableListOf<Post>()
    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    init {
        // Filter posts whenever query changes
        viewModelScope.launch {
            _currentQuery.collect { query ->
                filterPosts(query)
            }
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = ContentUiState.Loading
            try {
                val posts = ContentApiClient.api.getPosts(token)
                _fullPostList.clear()
                _fullPostList.addAll(posts)
                filterPosts(_currentQuery.value)
            } catch (e: Exception) {
                _uiState.value = ContentUiState.Error("Failed to load posts. Please check your connection.")
            }
        }
    }

    fun updateQuery(query: String) {
        _currentQuery.value = query
    }

    private fun filterPosts(query: String) {
        val filtered = if (query.isEmpty()) {
            _fullPostList
        } else {
            _fullPostList.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            if (query.isEmpty()) {
                // No posts at all
                _uiState.value = ContentUiState.Error("No posts found")
            } else {
                // No match for search query
                _uiState.value = ContentUiState.Error("No posts match your search")
            }
        } else {
            _uiState.value = ContentUiState.Success(filtered)
        }
    }

}
class ContentViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentViewModel(token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
