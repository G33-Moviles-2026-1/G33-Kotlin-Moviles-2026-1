package com.example.andespace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.model.HomeSearchParams
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
        _uiState.update { state ->
            val newState = state.copy(currentDestination = destination)
            if (destination == AppDestinations.CLASSROOMS) {
                newState.copy(contentScreen = ContentScreen.HOME)
            } else {
                newState
            }
        }
    }

    fun onHistoryClick() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HISTORY) }
    }

    fun onSearchClick(params: HomeSearchParams) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            repository.trackHomeEvent("home_search_submitted")
            repository.searchRooms(params)
                .fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                contentScreen = ContentScreen.RESULTS,
                                searchResults = response.rooms,
                                searchError = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                searchError = e.message ?: "Error de búsqueda"
                            )
                        }
                    }
                )
        }
    }

    fun navigateBackToHome() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HOME) }
    }

    fun onLogOut() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun onLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }

    fun onFiltersOpened() {
        viewModelScope.launch { repository.trackHomeEvent("home_filters_opened") }
    }
}
