package com.example.video_player_hub.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.video_player_hub.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val name: String, val email: String) : ProfileUiState()
    object LoggedOut : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        // Simulate loading user data
        if (token.isEmpty()) {
            _uiState.value = ProfileUiState.Error("User not authenticated")
            return
        }
        // Hardcoded example
        _uiState.value = ProfileUiState.Success(name = "Steven", email = "user@example.com")
    }

    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _uiState.value = ProfileUiState.LoggedOut
    }
}


class ProfileViewModelFactory(
    private val token: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}