package com.example.andespace.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AccountRepository
import com.example.andespace.data.repository.FriendsRepository
import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.ui.common.SnackbarManager
import com.example.andespace.ui.common.UserMessages
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
    private val repository: FriendsRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (_, status) = accountRepository.loadCachedProfile()
            _uiState.update { it.copy(myStatus = status) }
        }
        viewModelScope.launch {
            accountRepository.observeStatus().collect { status ->
                _uiState.update { it.copy(myStatus = status) }
            }
        }
        refreshAll()
    }

    fun refreshAll() {
        loadFriends()
        loadIncomingRequests()
        loadSuggestions()
    }

    fun openMyFriends() {
        _uiState.update { it.copy(contentScreen = FriendsContentScreen.MY_FRIENDS) }
    }

    fun openAddFriends() {
        _uiState.update { it.copy(contentScreen = FriendsContentScreen.ADD_FRIENDS) }
        loadSuggestions()
        loadIncomingRequests()
    }

    fun showStatusPicker() {
        _uiState.update { it.copy(showStatusPicker = true) }
    }

    fun dismissStatusPicker() {
        _uiState.update { it.copy(showStatusPicker = false) }
    }

    fun changeMyStatus(status: UserStatus) {
        viewModelScope.launch {
            accountRepository.changeStatus(status).fold(
                onSuccess = {
                    _uiState.update { it.copy(myStatus = status, showStatusPicker = false) }
                    SnackbarManager.showMessage("Status updated")
                },
                onFailure = { showBackendError(it) }
            )
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFriends = true, friendsError = null) }
            repository.getMyFriends()
                .onSuccess { friends ->
                    _uiState.update { it.copy(isLoadingFriends = false, friendsList = friends) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingFriends = false, friendsError = error.message) }
                    showBackendError(error)
                }
        }
    }

    fun loadIncomingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRequests = true) }
            repository.getIncomingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(isLoadingRequests = false, incomingRequests = requests) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingRequests = false) }
                    showBackendError(error)
                }
        }
    }

    fun loadSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSuggestions = true) }
            repository.getSuggestions()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoadingSuggestions = false, suggestions = list) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingSuggestions = false) }
                    showBackendError(error)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun sendFriendRequest() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        val email = if (query.contains("@")) query else "$query@uniandes.edu.co"

        viewModelScope.launch {
            repository.sendFriendRequest(email)
                .onSuccess {
                    val username = email.substringBefore("@")
                    val outgoing = OutgoingFriendRequest(email = email, username = username)
                    _uiState.update { state ->
                        state.copy(
                            searchQuery = "",
                            outgoingRequests = state.outgoingRequests
                                .filter { it.email != email } + outgoing
                        )
                    }
                    SnackbarManager.showMessage("Friend request sent")
                    loadSuggestions()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun sendSuggestionRequest(username: String) {
        _uiState.update { it.copy(searchQuery = username) }
        sendFriendRequest()
    }

    fun acceptFriendRequest(email: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(email)
                .onSuccess {
                    SnackbarManager.showMessage("Friend request accepted")
                    loadIncomingRequests()
                    loadFriends()
                    loadSuggestions()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun declineFriendRequest(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(incomingRequests = state.incomingRequests.filter { it.email != email })
                    }
                    loadSuggestions()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun cancelOutgoingRequest(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(outgoingRequests = state.outgoingRequests.filter { it.email != email })
                    }
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun removeFriend(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            friendsList = state.friendsList.filter { it.email != email },
                            incomingRequests = state.incomingRequests.filter { it.email != email },
                            outgoingRequests = state.outgoingRequests.filter { it.email != email }
                        )
                    }
                    loadSuggestions()
                }
                .onFailure { showBackendError(it) }
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
            it.copy(selectedFriend = null, friendScheduleData = null, scheduleError = null)
        }
    }

    private fun loadFriendSchedule() {
        val email = _uiState.value.selectedFriend?.email ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSchedule = true, scheduleError = null, friendScheduleData = null) }
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val dateString = _uiState.value.currentWeekDate.format(formatter)
            repository.getFriendWeeklySchedule(email, dateString)
                .onSuccess { schedule ->
                    _uiState.update { it.copy(isLoadingSchedule = false, friendScheduleData = schedule) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingSchedule = false, scheduleError = error.message) }
                    showBackendError(error)
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

    private fun showBackendError(error: Throwable) {
        SnackbarManager.showMessage(error.message ?: UserMessages.UNKNOWN_ERROR)
    }
}
