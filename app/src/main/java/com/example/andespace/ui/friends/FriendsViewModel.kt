package com.example.andespace.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.AccountRepository
import com.example.andespace.data.repository.AuthRepository
import com.example.andespace.data.repository.FriendsLocalSnapshot
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
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            applyLocalSnapshot(repository.loadLocalSnapshot())
        }
        viewModelScope.launch {
            accountRepository.observeStatus().collect { status ->
                _uiState.update { it.copy(myStatus = status) }
            }
        }
        viewModelScope.launch {
            var wasOnline = NetworkMonitor.isOnline.value
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline && !wasOnline && authRepository.hasLocalSession()) {
                    viewModelScope.launch {
                        repository.syncPendingFriendActions()
                        refreshAll()
                    }
                }
                wasOnline = isOnline
            }
        }
        viewModelScope.launch {
            if (authRepository.hasLocalSession()) {
                refreshAll()
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            applyLocalSnapshot(repository.loadLocalSnapshot())
            _uiState.update {
                it.copy(
                    isLoadingFriends = true,
                    isLoadingRequests = true,
                    isLoadingSuggestions = true,
                    friendsError = null
                )
            }
            repository.syncPendingFriendActions()
            repository.refreshAllParallel()
                .onSuccess { bundle ->
                    repository.persistNetworkBundle(bundle)
                    val local = repository.loadLocalSnapshot()
                    _uiState.update {
                        it.copy(
                            friendsList = local.friends,
                            incomingRequests = local.incoming,
                            outgoingRequests = local.outgoing,
                            suggestions = local.suggestions,
                            isLoadingFriends = false,
                            isLoadingRequests = false,
                            isLoadingSuggestions = false,
                            friendsError = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingFriends = false,
                            isLoadingRequests = false,
                            isLoadingSuggestions = false,
                            friendsError = if (it.friendsList.isEmpty()) error.message else null
                        )
                    }
                    if (_uiState.value.friendsList.isEmpty()) {
                        showBackendError(error)
                    }
                }
        }
    }

    fun openMyFriends() {
        _uiState.update { it.copy(contentScreen = FriendsContentScreen.MY_FRIENDS) }
    }

    fun openAddFriends() {
        viewModelScope.launch {
            val draft = repository.getSearchDraft()
            _uiState.update {
                it.copy(
                    contentScreen = FriendsContentScreen.ADD_FRIENDS,
                    searchQuery = draft
                )
            }
        }
        viewModelScope.launch {
            if (authRepository.hasLocalSession()) {
                refreshAll()
            }
        }
    }

    fun showStatusPicker() {
        _uiState.update {
            it.copy(
                showStatusPicker = true,
                statusPickerSelection = it.myStatus
            )
        }
    }

    fun dismissStatusPicker() {
        val selection = _uiState.value.statusPickerSelection
        val current = _uiState.value.myStatus
        if (selection == current) {
            _uiState.update { it.copy(showStatusPicker = false) }
            return
        }
        applyMyStatus(selection, closePickerOnSuccess = true)
    }

    fun cycleStatusPickerPrevious() {
        cycleStatusPicker(delta = -1)
    }

    fun cycleStatusPickerNext() {
        cycleStatusPicker(delta = 1)
    }

    private fun cycleStatusPicker(delta: Int) {
        val entries = UserStatus.entries
        val current = _uiState.value.statusPickerSelection
        val index = entries.indexOf(current).coerceAtLeast(0)
        val nextIndex = (index + delta + entries.size) % entries.size
        _uiState.update { it.copy(statusPickerSelection = entries[nextIndex]) }
    }

    private fun applyMyStatus(status: UserStatus, closePickerOnSuccess: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingStatus = true) }
            repository.changeMyStatus(status).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            myStatus = status,
                            isApplyingStatus = false,
                            showStatusPicker = if (closePickerOnSuccess) false else it.showStatusPicker
                        )
                    }
                    SnackbarManager.showMessage("Status updated")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isApplyingStatus = false) }
                    showBackendError(error)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            repository.saveSearchDraft(query)
        }
    }

    fun sendFriendRequest() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        val email = if (query.contains("@")) query else "$query@uniandes.edu.co"

        viewModelScope.launch {
            repository.sendFriendRequest(email)
                .onSuccess {
                    _uiState.update { it.copy(searchQuery = "") }
                    SnackbarManager.showMessage("Friend request sent")
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                    refreshAll()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun sendSuggestionRequest(username: String) {
        val email = if (username.contains("@")) username.trim() else "${username.trim()}@uniandes.edu.co"
        viewModelScope.launch {
            repository.sendFriendRequest(email)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            suggestions = state.suggestions.filterNot { suggestion ->
                                suggestion.equals(username, ignoreCase = true) ||
                                    suggestion.equals(email.substringBefore("@"), ignoreCase = true)
                            }
                        )
                    }
                    SnackbarManager.showMessage("Friend request sent")
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun acceptFriendRequest(email: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(email)
                .onSuccess {
                    SnackbarManager.showMessage("Friend request accepted")
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                    refreshAll()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun declineFriendRequest(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                    refreshAll()
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun cancelOutgoingRequest(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                }
                .onFailure { showBackendError(it) }
        }
    }

    fun removeFriend(email: String) {
        viewModelScope.launch {
            repository.deleteFriendship(email)
                .onSuccess {
                    applyLocalSnapshot(repository.loadLocalSnapshot())
                    refreshAll()
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
                    _uiState.update { it.copy(isLoadingSchedule = false, scheduleError = error.message, selectedFriend = null) }
                    SnackbarManager.showMessage("Friend's Schedule is not available")
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

    private fun applyLocalSnapshot(snapshot: FriendsLocalSnapshot) {
        _uiState.update { state ->
            state.copy(
                friendsList = snapshot.friends,
                incomingRequests = snapshot.incoming,
                outgoingRequests = snapshot.outgoing,
                suggestions = snapshot.suggestions,
                myStatus = snapshot.myStatus,
                searchQuery = if (state.contentScreen == FriendsContentScreen.ADD_FRIENDS) {
                    snapshot.searchDraft
                } else {
                    state.searchQuery
                }
            )
        }
    }

    private fun showBackendError(error: Throwable) {
        SnackbarManager.showMessage(error.message ?: UserMessages.UNKNOWN_ERROR)
    }
}
