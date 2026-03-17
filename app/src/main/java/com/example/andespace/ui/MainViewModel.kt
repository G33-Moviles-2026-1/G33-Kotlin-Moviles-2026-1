package com.example.andespace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.AppDestinations
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
        viewModelScope.launch {}
    }

    fun onDestinationChanged(destination: AppDestinations) {
        _uiState.update { it.copy(currentDestination = destination, isUserMenuExpanded = false) }
    }

    fun expandUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = true) }
    }

    fun closeUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = false) }
    }

    fun onLogOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.logout()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    isUserMenuExpanded = false,
                    isLoading = false,
                    currentDestination = AppDestinations.LOGIN
                )
            }
        }
    }

    fun onLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }
}
