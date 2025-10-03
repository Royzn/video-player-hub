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

sealed class ContentDetailUiState {
    object Loading : ContentDetailUiState()
    data class Success(val post: Post) : ContentDetailUiState()
    data class Error(val message: String) : ContentDetailUiState()
}

class ContentDetailViewModel(
    private val token: String,
    private val state: SavedStateHandle // add SavedStateHandle here
) : ViewModel() {

    companion object {
        private const val PLAYBACK_POSITION_KEY = "playback_position"
    }

    private val _uiState = MutableStateFlow<ContentDetailUiState>(ContentDetailUiState.Loading)
    val uiState: StateFlow<ContentDetailUiState> = _uiState

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
                _uiState.value = ContentDetailUiState.Success(post)
            } catch (e: Exception) {
                _uiState.value = ContentDetailUiState.Error("Failed to load post: ${e.message}")
            }
        }
    }
}


class ContentDetailViewModelFactory(
    private val token: String,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ContentDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentDetailViewModel(token, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
