package com.example.andespace.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.model.dto.BookingDto
import com.example.andespace.data.model.dto.CreateBookingRequest
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingsViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUIState())
    val uiState: StateFlow<BookingsUIState> = _uiState.asStateFlow()

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, requiresLogin = false) }
            repository.getMyBookings()
                .onSuccess { bookings ->
                    _uiState.update {
                        it.copy(bookings = bookings, isLoading = false)
                    }
                }
                .onFailure { error ->
                    val isSessionExpired = error.message == "SESSION_EXPIRED"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requiresLogin = isSessionExpired,
                            errorMessage = if (isSessionExpired) null else error.message
                        )
                    }
                }
        }
    }

    fun onEditBooking(booking: BookingDto) {
        _uiState.update {
            it.copy(
                selectedBooking = booking,
                contentScreen = BookingsContentScreen.EDIT
            )
        }
    }

    fun onDeleteBooking(booking: BookingDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.deleteBooking(booking.id)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            bookings = state.bookings.filter { it.id != booking.id },
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun onSaveBooking(request: CreateBookingRequest, oldBookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            repository.deleteBooking(oldBookingId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = "Delete failed: ${error.message}")
                    }
                    return@launch
                }

            repository.createBooking(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            selectedBooking = null,
                            isSaving = false,
                            contentScreen = BookingsContentScreen.LIST
                        )
                    }
                    loadBookings()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = "Create failed: ${error.message}")
                    }
                    loadBookings()
                }
        }
    }

    fun onCancelEdit() {
        _uiState.update {
            it.copy(
                selectedBooking = null,
                contentScreen = BookingsContentScreen.LIST
            )
        }
    }

    fun onCreateBooking(request: CreateBookingRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            repository.createBooking(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(isCreating = false, bookingCreatedSuccess = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isCreating = false, createError = error.message)
                    }
                }
        }
    }

    fun consumeBookingCreatedSuccess() {
        _uiState.update { it.copy(bookingCreatedSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, createError = null) }
    }
}
