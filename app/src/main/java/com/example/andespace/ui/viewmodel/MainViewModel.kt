package com.example.andespace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.state.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val name = repository.getUserName()
            _uiState.update {
                it.copy(userName = name, isLoading = false)
            }
        }
    }

    fun onDestinationChanged(destination: AppDestinations) {
        _uiState.update { it.copy(currentDestination = destination) }
    }

    fun onHistoryClick() {
        _uiState.update { it.copy(currentDestination = AppDestinations.HISTORY) }
    }

    fun onLogOut() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun onLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }
}
