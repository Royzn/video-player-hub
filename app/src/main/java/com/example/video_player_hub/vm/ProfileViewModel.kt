package com.example.video_player_hub.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.video_player_hub.util.TokenManager
import com.example.video_player_hub.room.WatchList
import com.example.video_player_hub.room.WatchListDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val name: String, val email: String, val watchlist: List<WatchList>) : ProfileUiState()
    object LoggedOut : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val token: String, private val watchListDao: WatchListDao) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _watchlist = MutableStateFlow<List<WatchList>>(emptyList())
    val watchlist: StateFlow<List<WatchList>> = _watchlist

    init {
        loadUser()
        loadWatchlist()  // Load the watchlist when the ViewModel is created
    }

    private fun loadUser() {
        if (token.isEmpty()) {
            _uiState.value = ProfileUiState.Error("User not authenticated")
            return
        }
        // Hardcoded user data
        _uiState.value = ProfileUiState.Success(name = "Steven", email = "user@example.com", watchlist = _watchlist.value)
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            try {
                watchListDao.getAll().collect { watchlist ->
                    _watchlist.value = watchlist  // Update the watchlist state
                    _uiState.value = ProfileUiState.Success(
                        name = "Steven", email = "user@example.com", watchlist = watchlist
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error loading watchlist: ${e.message}")
            }
        }
    }

    fun removeFromWatchlist(post: WatchList) {
        viewModelScope.launch {
            try {
                watchListDao.deleteById(post.id)  // Delete from DB
                val updatedWatchlist = _watchlist.value.toMutableList().apply {
                    remove(post)
                }
                _watchlist.value = updatedWatchlist  // Update local list
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error removing item from watchlist: ${e.message}")
            }
        }
    }

    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _uiState.value = ProfileUiState.LoggedOut
    }
}



class ProfileViewModelFactory(private val token: String, private val watchListDao: WatchListDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(token, watchListDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
