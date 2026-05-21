package com.example.andespace.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.FriendsRepository
import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.ui.common.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class FriendsViewModel(
    private val repository: FriendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadIncomingRequests()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFriends = true, friendsError = null) }
            val result = repository.getMyFriends()

            result.onSuccess { friends ->
                _uiState.update { it.copy(isLoadingFriends = false, friendsList = friends) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingFriends = false, friendsError = error.message) }
                SnackbarManager.showMessage(error.message ?: "Could not load friends")
            }
        }
    }

    fun loadIncomingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRequests = true) }
            val result = repository.getIncomingRequests()

            result.onSuccess { requests ->
                _uiState.update { it.copy(isLoadingRequests = false, incomingRequests = requests) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingRequests = false) }
            }
        }
    }

    fun onSearchEmailChange(email: String) {
        _uiState.update { it.copy(searchEmail = email) }
    }

    fun sendFriendRequest() {
        val email = _uiState.value.searchEmail.trim()
        if (email.isEmpty()) return

        viewModelScope.launch {
            val result = repository.sendFriendRequest(email)
            result.onSuccess {
                SnackbarManager.showMessage("Friend request sent to $email")
                _uiState.update { it.copy(searchEmail = "") }
            }.onFailure { error ->
                SnackbarManager.showMessage(error.message ?: "Failed to send request")
            }
        }
    }

    fun acceptFriendRequest(email: String) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(email)
            result.onSuccess {
                SnackbarManager.showMessage("Friend request accepted")
                loadIncomingRequests()
                loadFriends()
            }.onFailure { error ->
                SnackbarManager.showMessage(error.message ?: "Failed to accept request")
            }
        }
    }

    fun selectFriend(friend: FriendItemOut) {
        _uiState.update {
            it.copy(
                selectedFriend = friend,
                currentWeekDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            )
        }
        loadFriendSchedule()
    }

    fun clearSelectedFriend() {
        _uiState.update {
            it.copy(
                selectedFriend = null,
                friendScheduleData = null,
                scheduleError = null
            )
        }
    }

    private fun loadFriendSchedule() {
        val email = _uiState.value.selectedFriend?.email ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSchedule = true, scheduleError = null, friendScheduleData = null) }

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val requestedDate = _uiState.value.currentWeekDate
            val dateString = requestedDate.format(formatter)

            val result = repository.getFriendWeeklySchedule(email, dateString)

            result.onSuccess { schedule ->
                _uiState.update { it.copy(isLoadingSchedule = false, friendScheduleData = schedule) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingSchedule = false, scheduleError = error.message) }
                SnackbarManager.showMessage(error.message ?: "Could not load friend's schedule")
            }
        }
    }

    fun loadNextWeek() {
        _uiState.update { it.copy(currentWeekDate = it.currentWeekDate.plusDays(7)) }
        loadFriendSchedule()
    }

    fun loadPreviousWeek() {
        _uiState.update { it.copy(currentWeekDate = it.currentWeekDate.minusDays(7)) }
        loadFriendSchedule()
    }
}