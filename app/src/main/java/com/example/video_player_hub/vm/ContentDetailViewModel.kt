package com.example.video_player_hub.vm

import android.os.Bundle
import androidx.lifecycle.*
import com.example.video_player_hub.data.Post
import com.example.video_player_hub.network.ContentApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.video_player_hub.room.WatchListDao
import com.example.video_player_hub.room.WatchList

sealed class ContentDetailUiState {
    object Loading : ContentDetailUiState()
    data class Success(val post: Post) : ContentDetailUiState()
    data class Error(val message: String) : ContentDetailUiState()
}

class ContentDetailViewModel(
    private val token: String,
    private val state: SavedStateHandle,
    private val watchListDao: WatchListDao
) : ViewModel() {

    companion object {
        private const val PLAYBACK_POSITION_KEY = "playback_position"
    }

    private val _uiState = MutableStateFlow<ContentDetailUiState>(ContentDetailUiState.Loading)
    val uiState: StateFlow<ContentDetailUiState> = _uiState

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private var currentPost: Post? = null

    var playbackPosition: Long
        get() = state.get<Long>(PLAYBACK_POSITION_KEY) ?: 0L
        set(value) {
            state.set(PLAYBACK_POSITION_KEY, value)
        }

    fun loadPost(postId: Int) {
        _uiState.value = ContentDetailUiState.Loading
        viewModelScope.launch {
            try {
                val post = ContentApiClient.api.getPostById(token, postId)
                currentPost = post
                _uiState.value = ContentDetailUiState.Success(post)

                // Check if post is in watchlist
                val watchLaterItem = watchListDao.getByPostId(postId)
                _isFavorite.value = watchLaterItem != null
            } catch (e: Exception) {
                _uiState.value = ContentDetailUiState.Error("Failed to load post: ${e.message}")
            }
        }
    }

    fun toggleFavorite() {
        val post = currentPost ?: return
        viewModelScope.launch {
            val exists = watchListDao.getByPostId(post.id)
            if (exists != null) {
                watchListDao.deleteById(exists.id)
                _isFavorite.value = false
            } else {
                val item = WatchList(
                    id = post.id.toLong(),
                    title = post.title,
                    excerpt = post.body.take(100),
                    savedAt = System.currentTimeMillis()
                )
                watchListDao.insert(item)
                _isFavorite.value = true
            }
        }
    }
}



class ContentDetailViewModelFactory(
    private val token: String,
    private val owner: SavedStateRegistryOwner,
    private val watchListDao: WatchListDao,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ContentDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentDetailViewModel(token, handle, watchListDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

