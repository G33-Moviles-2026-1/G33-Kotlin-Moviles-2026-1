package com.example.andespace.ui.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomepageViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    fun resetToHome() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HOME) }
    }

    fun onShowResults() {
        _uiState.update { it.copy(contentScreen = ContentScreen.RESULTS) }
    }

    fun onShowRoomDetailScreen() {
        _uiState.update { it.copy(contentScreen = ContentScreen.ROOM_DETAIL) }
    }

    fun onFiltersOpened() {
        viewModelScope.launch { repository.trackHomeEvent("home_filters_opened") }
    }
}
