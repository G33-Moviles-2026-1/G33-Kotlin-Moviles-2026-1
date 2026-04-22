package com.example.andespace.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.repository.shared.RepositoryMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingsViewModel(private val repository: BookingRepository): ViewModel()  {
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
                            errorMessage = if (isSessionExpired) null else friendlyError(error.message)
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
                .onFailure { _ ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = RepositoryMessages.DELETE_BOOKING_FAILED)
                    }
                }
        }
    }

    fun onSaveBooking(request: CreateBookingRequest, oldBookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            repository.deleteBooking(oldBookingId)
                .onFailure { _ ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = RepositoryMessages.DELETE_BOOKING_FAILED)
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
                .onFailure { _ ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = RepositoryMessages.SAVE_BOOKING_FAILED)
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
                        it.copy(isCreating = false, createError = friendlyError(error.message))
                    }
                }
        }
    }

    fun consumeBookingCreatedSuccess() {
        _uiState.update { it.copy(bookingCreatedSuccess = false) }
    }


    fun resetRequiresLogin() {
        _uiState.update { it.copy(requiresLogin = false) }
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null -> RepositoryMessages.GENERIC_ERROR
        raw.startsWith("No internet connection") -> RepositoryMessages.NO_INTERNET
        raw.startsWith("Network error") -> RepositoryMessages.NO_INTERNET
        raw.matches(Regex("Error \\d+.*")) -> RepositoryMessages.GENERIC_ERROR
        else -> RepositoryMessages.GENERIC_ERROR
    }
}
