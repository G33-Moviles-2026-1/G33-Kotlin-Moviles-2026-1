package com.example.andespace.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.ui.common.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingsViewModel(private val repository: BookingRepository): ViewModel()  {
    private val _uiState = MutableStateFlow(BookingsUIState())
    val uiState: StateFlow<BookingsUIState> = _uiState.asStateFlow()

    init {
        observeBookings()
        loadBookings()
        observeNetwork()
    }

    private fun observeBookings() {
        viewModelScope.launch {
            repository.bookings.collect { bookings ->
                _uiState.update { it.copy(bookings = bookings) }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    refreshBookings()
                }
            }
        }
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, requiresLogin = false) }
            repository.getMyBookings()
                .onSuccess { bookings ->
                    _uiState.update {
                        it.copy(isLoading = false)
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

    private fun refreshBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.refreshBookings()
            _uiState.update { it.copy(isRefreshing = false) }
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
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { _ ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = UserMessages.DELETE_BOOKING_FAILED)
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
                        it.copy(isSaving = false, errorMessage = UserMessages.DELETE_BOOKING_FAILED)
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
                    if (error.message == "OFFLINE_SYNC_PENDING") {
                        _uiState.update {
                            it.copy(
                                selectedBooking = null,
                                isSaving = false,
                                contentScreen = BookingsContentScreen.LIST,
                                syncMessage = UserMessages.BOOKING_PENDING_SYNC
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isSaving = false, errorMessage = UserMessages.SAVE_BOOKING_FAILED)
                        }
                    }
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
                    if (error.message == "OFFLINE_SYNC_PENDING") {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                bookingCreatedSuccess = true, // We treat it as success to close the screen
                                syncMessage = UserMessages.BOOKING_PENDING_SYNC
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isCreating = false, createError = friendlyError(error.message))
                        }
                    }
                }
        }
    }

    fun consumeBookingCreatedSuccess() {
        _uiState.update { it.copy(bookingCreatedSuccess = false) }
    }

    fun consumeSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }


    fun resetRequiresLogin() {
        _uiState.update { it.copy(requiresLogin = false) }
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null -> UserMessages.GENERIC_ERROR
        raw == "OFFLINE_SYNC_PENDING" -> UserMessages.BOOKING_PENDING_SYNC
        raw.startsWith("No internet connection") -> UserMessages.NO_INTERNET
        raw.startsWith("Network error") -> UserMessages.NO_INTERNET
        raw.matches(Regex("Error \\d+.*")) -> UserMessages.GENERIC_ERROR
        else -> UserMessages.GENERIC_ERROR
    }
}
