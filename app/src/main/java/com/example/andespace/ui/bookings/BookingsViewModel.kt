package com.example.andespace.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.ui.common.SnackbarManager
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
            _uiState.update { it.copy(isLoading = true, requiresLogin = false) }
            repository.getMyBookings()
                .onSuccess { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isShowingCached = !NetworkMonitor.isOnline.value
                        )
                    }
                }
                .onFailure { error ->
                    val isSessionExpired = error.message == "SESSION_EXPIRED"
                    _uiState.update {
                        it.copy(isLoading = false, requiresLogin = isSessionExpired)
                    }
                    if (!isSessionExpired) {
                        SnackbarManager.showMessage(
                            error.message ?: RepositoryMessages.GENERIC_ERROR
                        )
                    }
                }
        }
    }

    private fun refreshBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.refreshBookings()
            _uiState.update { it.copy(isRefreshing = false, isShowingCached = false) }
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
            _uiState.update { it.copy(isLoading = true) }
            repository.deleteBooking(booking.id)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    if (error.message == "OFFLINE_SYNC_PENDING") {
                        SnackbarManager.showMessage(UserMessages.BOOKING_PENDING_SYNC)
                    } else {
                        SnackbarManager.showMessage(
                            error.message ?: RepositoryMessages.GENERIC_ERROR
                        )
                    }
                }
        }
    }

    fun onSaveBooking(request: CreateBookingRequest, oldBookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val deleteResult = repository.deleteBooking(oldBookingId)
            val deletePendingSync = deleteResult.exceptionOrNull()?.message == "OFFLINE_SYNC_PENDING"
            if (deleteResult.isFailure && !deletePendingSync) {
                _uiState.update { it.copy(isSaving = false) }
                SnackbarManager.showMessage(
                    deleteResult.exceptionOrNull()?.message ?: RepositoryMessages.GENERIC_ERROR
                )
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
                                contentScreen = BookingsContentScreen.LIST
                            )
                        }
                        SnackbarManager.showMessage(UserMessages.BOOKING_PENDING_SYNC)
                    } else {
                        _uiState.update { it.copy(isSaving = false) }
                        SnackbarManager.showMessage(
                            error.message ?: RepositoryMessages.GENERIC_ERROR
                        )
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
            _uiState.update { it.copy(isCreating = true) }
            repository.createBooking(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(isCreating = false, bookingCreatedSuccess = true)
                    }
                }
                .onFailure { error ->
                    if (error.message == "OFFLINE_SYNC_PENDING") {
                        _uiState.update {
                            it.copy(isCreating = false, bookingCreatedSuccess = true)
                        }
                        SnackbarManager.showMessage(UserMessages.BOOKING_PENDING_SYNC)
                    } else {
                        _uiState.update { it.copy(isCreating = false) }
                        SnackbarManager.showMessage(
                            error.message ?: RepositoryMessages.GENERIC_ERROR
                        )
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

}
