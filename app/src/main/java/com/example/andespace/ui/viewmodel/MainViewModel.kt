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

/**
 * El ViewModel actúa como intermediario.
 * Llama al repositorio para obtener datos y actualiza el State para la UI.
 */
class MainViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    // Estado único de la UI
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
        // Lógica para historial
        println("Historial clickeado")
    }

    fun onAccountClick() {
        // Lógica para cuenta
        println("Cuenta de: ${uiState.value.userName} clickeada")
    }
}
