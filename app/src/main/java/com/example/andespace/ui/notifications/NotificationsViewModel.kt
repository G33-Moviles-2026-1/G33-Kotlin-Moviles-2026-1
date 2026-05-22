package com.example.andespace.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
        loadNotifications()
        observeNetwork()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            repository.notifications.collect { dtos ->
                val models = dtos.map { it.toUiModel() }
                val unread = models.count { !it.isRead }
                _uiState.update { it.copy(notifications = models, unreadCount = unread) }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline && _uiState.value.isOffline) {
                    loadNotifications()
                }
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.refreshNotifications()
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isOffline = false) }
            }.onFailure { error ->
                val hasCached = repository.hasCachedNotifications()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOffline = hasCached,
                        errorMessage = if (hasCached) null else (error.message ?: "Could not load notifications.")
                    )
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            val result = repository.markAsRead(id)
            result.onFailure {
                _uiState.update { it.copy(errorMessage = "Could not update notification. Try again when online.") }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead()
            result.onSuccess {
                _uiState.update { it.copy(successMessage = "All notifications marked as read.") }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "Could not update. Please try again.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
