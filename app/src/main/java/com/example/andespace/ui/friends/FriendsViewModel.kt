package com.example.andespace.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AccountRepository
import com.example.andespace.data.repository.FriendsRepository
import com.example.andespace.model.dto.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val friendsRepository: FriendsRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (_, status) = accountRepository.loadCachedProfile()
            _uiState.update { it.copy(myStatus = status) }
        }
        viewModelScope.launch {
            accountRepository.observeStatus().collect { status ->
                _uiState.update { it.copy(myStatus = status) }
            }
        }
    }

    fun onScreenOpened() {
        refreshFriends()
    }

    fun refreshFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            friendsRepository.getMyFriends().fold(
                onSuccess = { friends ->
                    _uiState.update { it.copy(friends = friends, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not load friends."
                        )
                    }
                }
            )
        }
    }

    fun showAddFriendDialog() {
        _uiState.update { it.copy(showAddFriendDialog = true, errorMessage = null) }
    }

    fun dismissAddFriendDialog() {
        _uiState.update { it.copy(showAddFriendDialog = false) }
    }

    fun showStatusPicker() {
        _uiState.update { it.copy(showStatusPicker = true) }
    }

    fun dismissStatusPicker() {
        _uiState.update { it.copy(showStatusPicker = false) }
    }

    fun addFriend(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingFriend = true, errorMessage = null) }
            friendsRepository.addFriend(username).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isAddingFriend = false,
                            showAddFriendDialog = false,
                            successMessage = "Friend added."
                        )
                    }
                    refreshFriends()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAddingFriend = false,
                            errorMessage = error.message ?: "Could not add friend."
                        )
                    }
                }
            )
        }
    }

    fun deleteFriend(username: String) {
        viewModelScope.launch {
            friendsRepository.deleteFriend(username).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            friends = state.friends.filter { it.username != username },
                            successMessage = "Friend removed."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not remove friend.")
                    }
                }
            )
        }
    }

    fun changeMyStatus(status: UserStatus) {
        viewModelScope.launch {
            accountRepository.changeStatus(status).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(myStatus = status, showStatusPicker = false, successMessage = "Status updated.")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not update status.")
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
