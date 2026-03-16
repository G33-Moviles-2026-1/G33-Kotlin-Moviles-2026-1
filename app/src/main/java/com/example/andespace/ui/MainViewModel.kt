package com.example.andespace.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.model.HomeSearchParams
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private var lastSearchParams: HomeSearchParams? = null

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {}
    }

    fun onDestinationChanged(destination: AppDestinations) {
        _uiState.update { state ->
            val newState = state.copy(currentDestination = destination)
            if (destination == AppDestinations.CLASSROOMS) {
                newState.copy(contentScreen = ContentScreen.HOME)
            } else {
                newState
            }
        }
    }

    fun onHistoryClick() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HISTORY) }
    }

    fun onSearchClick(params: HomeSearchParams) {
        lastSearchParams = params
        requestSearchPage(params = params, page = 1, trackEvent = true)
    }

    fun navigateBackToHome() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HOME) }
    }

    fun onNextResultsPage() {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val nextPage = (state.currentResultsPage + 1).coerceAtMost(state.totalResultsPages)
        if (nextPage == state.currentResultsPage) return

        requestSearchPage(params = params, page = nextPage)
    }

    fun onPreviousResultsPage() {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val previousPage = (state.currentResultsPage - 1).coerceAtLeast(1)
        if (previousPage == state.currentResultsPage) return

        requestSearchPage(params = params, page = previousPage)
    }

    private fun requestSearchPage(
        params: HomeSearchParams,
        page: Int,
        trackEvent: Boolean = false
    ) {
        viewModelScope.launch {
            val pageSize = _uiState.value.resultsPageSize
            val offset = (page - 1).coerceAtLeast(0) * pageSize

            Log.d(
                TAG,
                "requestSearchPage params -> page=$page, limit=$pageSize, offset=$offset, classroom=${params.classroom}, date=${params.date}, since=${params.since}, until=${params.until}, closeToMe=${params.closeToMe}, utilities=${params.utilities}"
            )

            _uiState.update { it.copy(isSearching = true, searchError = null) }
            if (trackEvent) {
                repository.trackHomeEvent("home_search_submitted")
            }

            repository.searchRooms(params, limit = pageSize, offset = offset)
                .fold(
                    onSuccess = { response ->
                        val totalItems = response.total ?: (offset + response.rooms.size)
                        val pages = calculateTotalPages(
                            totalItems = totalItems,
                            pageSize = pageSize
                        )

                        Log.d(
                            TAG,
                            "requestSearchPage success -> page=$page, rooms=${response.rooms.size}, totalItems=$totalItems, totalPages=$pages"
                        )

                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                contentScreen = ContentScreen.RESULTS,
                                searchResults = response.rooms,
                                currentResultsPage = page,
                                totalResultsPages = pages,
                                searchError = null
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "requestSearchPage failed -> ${e.message}", e)
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                searchError = e.message ?: "Search error"
                            )
                        }
                    }
                )
        }
    }

    fun onLogOut() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun onLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }

    fun onFiltersOpened() {
        viewModelScope.launch { repository.trackHomeEvent("home_filters_opened") }
    }

    private fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        if (totalItems <= 0) return 1
        return ((totalItems + pageSize - 1) / pageSize).coerceAtLeast(1)
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
