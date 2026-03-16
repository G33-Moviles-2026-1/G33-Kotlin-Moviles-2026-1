package com.example.andespace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.AppDestinations
import com.example.andespace.model.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            bookings = listOf(
                Booking(id = 1, roomName = "ML 517", sinceHour = 11, sinceMinute = 0, untilHour = 12, untilMinute = 30),
                Booking(id = 2, roomName = "LL 301", sinceHour = 16, sinceMinute = 0, untilHour = 17, untilMinute = 30),
                Booking(id = 3, roomName = "LL 302", sinceHour = 14, sinceMinute = 0, untilHour = 15, untilMinute = 30)
            )
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {}
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

    fun onEditBooking(booking: Booking) {
        _uiState.update {
            it.copy(
                selectedBooking = booking,
                currentDestination = AppDestinations.EDIT_BOOKING
            )
        }
    }

    fun onDeleteBooking(booking: Booking) {
        _uiState.update {
            it.copy(bookings = it.bookings.filter { b -> b.id != booking.id })
        }
    }

    fun onSaveBooking(updated: Booking) {
        _uiState.update { state ->
            state.copy(
                bookings = state.bookings.map { if (it.id == updated.id) updated else it },
                selectedBooking = null,
                currentDestination = AppDestinations.BOOKINGS
            )
        }
    }

    fun onCancelEdit() {
        _uiState.update {
            it.copy(
                selectedBooking = null,
                currentDestination = AppDestinations.BOOKINGS
            )
        }
    }
}
