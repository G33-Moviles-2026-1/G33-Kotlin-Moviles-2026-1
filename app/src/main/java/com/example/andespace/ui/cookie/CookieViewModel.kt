package com.example.andespace.ui.cookie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CookieUiState(
    val data: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CookieViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CookieUiState())
    val uiState: StateFlow<CookieUiState> = _uiState.asStateFlow()

    init {
        fetchMyData()
    }

    fun fetchMyData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, data = "") }

            val result = repository.getMeData()

            result.onSuccess { backendData ->
                _uiState.update { it.copy(isLoading = false, data = backendData) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not fetch session data"
                    )
                }
            }
        }
    }
}