package com.example.andespace.ui.results

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.RoomRepository
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val repository: RoomRepository,
    private val analyticsRepository: AnalyticsRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private var lastSearchParams: HomeSearchParams? = null

    private var cachedParams: HomeSearchParams? = null
    private var cachedTotalPages: Int = 1
    private var cachedHasUploadedSchedule: Boolean = false
    private val cachedPages = mutableMapOf<Int, List<RoomDto>>()

    private fun clearCache() {
        cachedParams = null
        cachedTotalPages = 1
        cachedHasUploadedSchedule = false
        cachedPages.clear()
    }

    fun onSearchClick(
        params: HomeSearchParams,
        isUserLoggedIn: Boolean,
        onNavigateToResults: (Boolean) -> Unit = {}
    ) {
        lastSearchParams = params
        requestSearchPage(
            params = params,
            page = 1,
            isUserLoggedIn = isUserLoggedIn,
            trackEvent = true,
            fromHomepageAttempt = true,
            onNavigateToResults = onNavigateToResults
        )
    }

    fun onRoomClick(room: RoomDto) {
        _uiState.update {
            it.copy(selectedRoom = room)
        }
    }

    fun onNextPage(isUserLoggedIn: Boolean) {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val nextPage = (state.currentPage + 1).coerceAtMost(state.totalPages)
        if (nextPage == state.currentPage) return

        requestSearchPage(params = params, page = nextPage, isUserLoggedIn = isUserLoggedIn)
    }

    fun onPreviousPage(isUserLoggedIn: Boolean) {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val previousPage = (state.currentPage - 1).coerceAtLeast(1)
        if (previousPage == state.currentPage) return

        requestSearchPage(params = params, page = previousPage, isUserLoggedIn = isUserLoggedIn)
    }

    private fun requestSearchPage(
        params: HomeSearchParams,
        page: Int,
        isUserLoggedIn: Boolean,
        trackEvent: Boolean = false,
        fromHomepageAttempt: Boolean = false,
        onNavigateToResults: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val pageSize = _uiState.value.resultsPageSize
            val offset = (page - 1).coerceAtLeast(0) * pageSize

            Log.d(
                TAG,
                "requestSearchPage -> page=$page, limit=$pageSize, offset=$offset, classroom=${params.classroom}, date=${params.date}, since=${params.since}, until=${params.until}, closeToMe=${params.closeToMe}, utilities=${params.utilities}"
            )

            _uiState.update { it.copy(isSearching = true, errorMessage = null) }

            if (trackEvent) {
                analyticsRepository.trackHomeEvent("home_search_submitted")
                analyticsRepository.trackAppliedFilters(
                    placeUsed = params.classroom.isNotBlank(),
                    timeUsed = params.since != null || params.until != null,
                    utilitiesUsed = params.utilities.isNotEmpty(),
                    closeToMeUsed = params.closeToMe
                )
            }

            repository.searchRooms(params, limit = pageSize, offset = offset)
                .fold(
                    onSuccess = { response ->
                        val totalItems = response.total ?: (offset + response.rooms.size)
                        val pages = calculateTotalPages(totalItems = totalItems, pageSize = pageSize)
                        val rooms = response.rooms


                        if (page == 1) {
                            // Replace snapshot only after a successful new search.
                            clearCache()
                            cachedParams = params
                            cachedTotalPages = pages
                            cachedPages[1] = rooms
                            prefetchSnapshotPages(params = params, isUserLoggedIn = isUserLoggedIn)
                        } else if (cachedParams == params && page in 1..MAX_CACHED_PAGE) {
                            cachedPages[page] = rooms
                        }
                        Log.d(TAG, "requestSearchPage -> snapshot cache pages=${cachedPages.keys.sorted()}")

                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                rooms = rooms,
                                selectedSearchDate = params.date,
                                currentPage = page,
                                totalPages = pages,
                                errorMessage = null,
                                showingCachedResults = false
                            )
                        }
                        if (fromHomepageAttempt && page == 1) {
                            onNavigateToResults(true)
                        }
                    },
                    onFailure = { error ->
                        val connectivityFailure = isConnectivityFailure(error.message)
                        val cachedRooms = cachedPages[page]
                        val canServeFromSnapshot = cachedRooms != null &&
                            cachedParams != null &&
                            page in 1..MAX_CACHED_PAGE

                        if (fromHomepageAttempt && page == 1 && connectivityFailure) {
                            val snapshotPageOne = cachedPages[1]
                            if (snapshotPageOne != null) {
                                Log.d(TAG, "requestSearchPage -> homepage offline fallback using snapshot page 1")
                                _uiState.update {
                                    it.copy(
                                        isSearching = false,
                                        rooms = snapshotPageOne,
                                        hasUploadedSchedule = cachedHasUploadedSchedule,
                                        selectedSearchDate = cachedParams?.date ?: params.date,
                                        currentPage = 1,
                                        totalPages = cachedTotalPages,
                                        errorMessage = "No internet connection. Showing the cached results from your last search.",
                                        showingCachedResults = true
                                    )
                                }
                                onNavigateToResults(true)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isSearching = false,
                                        errorMessage = "No internet connection. Please check your connection and try again.",
                                        showingCachedResults = false
                                    )
                                }
                                onNavigateToResults(false)
                            }
                            return@fold
                        }

                        if (connectivityFailure && page > MAX_CACHED_PAGE) {
                            _uiState.update {
                                it.copy(
                                    isSearching = false,
                                    errorMessage = "More results require an internet connection. Please check your connection and try again.",
                                    showingCachedResults = false
                                )
                            }
                            return@fold
                        }

                        if (canServeFromSnapshot) {
                            Log.d(TAG, "requestSearchPage -> offline, serving snapshot page $page from cache (${cachedRooms.size} rooms)")
                            _uiState.update {
                                it.copy(
                                    isSearching = false,
                                    rooms = cachedRooms,
                                    hasUploadedSchedule = cachedHasUploadedSchedule,
                                    selectedSearchDate = params.date,
                                    currentPage = page,
                                    totalPages = cachedTotalPages,
                                    errorMessage = "No internet connection. Showing cached results for page $page.",
                                    showingCachedResults = true
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isSearching = false,
                                    errorMessage = friendlyError(error.message),
                                    showingCachedResults = false
                                )
                            }
                        }
                    }
                )
        }
    }

    private fun prefetchSnapshotPages(params: HomeSearchParams, isUserLoggedIn: Boolean) {
        (2..MAX_CACHED_PAGE).forEach { page ->
            viewModelScope.launch {
                val pageSize = _uiState.value.resultsPageSize
                val offset = (page - 1) * pageSize
                repository.searchRooms(params, limit = pageSize, offset = offset).fold(
                    onSuccess = { response ->
                        val rooms = response.rooms
                        if (cachedParams == params) {
                            cachedPages[page] = rooms
                            Log.d(TAG, "prefetchSnapshotPages -> cached page $page")
                        }
                    },
                    onFailure = {
                        // Silent by design: prefetch failure must not break UX.
                    }
                )
            }
        }
    }

    private fun isConnectivityFailure(raw: String?): Boolean {
        if (raw.isNullOrBlank()) return false
        return raw.contains("internet", ignoreCase = true) ||
            raw.contains("network", ignoreCase = true) ||
            raw.contains("timeout", ignoreCase = true)
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null -> "Something went wrong. Please try again."
        raw.startsWith("No internet connection") -> "No internet connection. Please check your network and try again."
        raw.startsWith("Network error") -> "No internet connection. Please check your network and try again."
        raw.matches(Regex("Error \\d+.*")) -> "Could not load results. Please try again."
        else -> "Something went wrong. Please try again."
    }

    private fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        if (totalItems <= 0) return 1
        return ((totalItems + pageSize - 1) / pageSize).coerceAtLeast(1)
    }

    companion object {
        private const val TAG = "ResultsViewModel"
        private const val MAX_CACHED_PAGE = 3
    }
}
