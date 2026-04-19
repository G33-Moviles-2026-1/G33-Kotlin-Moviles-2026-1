package com.example.andespace.ui.results

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.RoomTimeWindowDto
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

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

    fun onSearchClick(params: HomeSearchParams, isUserLoggedIn: Boolean) {
        lastSearchParams = params
        clearCache()
        requestSearchPage(params = params, page = 1, trackEvent = true)
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

        requestSearchPage(params = params, page = nextPage)
    }

    fun onPreviousPage(isUserLoggedIn: Boolean) {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val previousPage = (state.currentPage - 1).coerceAtLeast(1)
        if (previousPage == state.currentPage) return

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
                "requestSearchPage -> page=$page, limit=$pageSize, offset=$offset, classroom=${params.classroom}, date=${params.date}, since=${params.since}, until=${params.until}, closeToMe=${params.closeToMe}, utilities=${params.utilities}"
            )

            _uiState.update { it.copy(isSearching = true, errorMessage = null) }

            if (trackEvent) {
                repository.trackHomeEvent("home_search_submitted")
                repository.trackAppliedFilters(
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

                        cachedParams = params
                        cachedTotalPages = pages

                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                rooms = response.rooms,
                                selectedSearchDate = params.date,
                                currentPage = page,
                                totalPages = pages,
                                errorMessage = null,
                                showingCachedResults = false
                            )
                        }
                    },
                    onFailure = { error ->
                        val cachedRooms = cachedPages[page]
                        if (cachedRooms != null && cachedParams == params) {
                            Log.d(TAG, "requestSearchPage -> no network, serving page $page from cache (${cachedRooms.size} rooms)")
                            _uiState.update {
                                it.copy(
                                    isSearching = false,
                                    rooms = cachedRooms,
                                    selectedSearchDate = params.date,
                                    currentPage = page,
                                    totalPages = cachedTotalPages,
                                    errorMessage = null,
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

    private fun friendlyError(raw: String?): String = when {
        raw == null -> "Something went wrong. Please try again."
        raw.startsWith("No internet connection") -> "No internet connection. Please check your network and try again."
        raw.startsWith("Network error") -> "No internet connection. Please check your network and try again."
        raw.matches(Regex("Error \\d+.*")) -> "Could not load results. Please try again."
        else -> "Something went wrong. Please try again."
    }

    private fun String?.toSecondsOfDay(): Int? {
        if (this.isNullOrBlank()) return null
        val normalized = this.trim().substringBefore('.')
        val parts = normalized.split(':')
        if (parts.size < 2) return null

        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        val seconds = parts.getOrNull(2)?.toIntOrNull() ?: 0

        if (hours !in 0..23 || minutes !in 0..59 || seconds !in 0..59) return null
        return hours * 3600 + minutes * 60 + seconds
    }

    private fun Int.toTimeString(): String {
        val hours = this / 3600
        val minutes = (this % 3600) / 60
        val seconds = this % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun intervalDuration(window: RoomTimeWindowDto): Int {
        val start = window.start.toSecondsOfDay() ?: return 0
        val end = window.end.toSecondsOfDay() ?: return 0
        return (end - start).coerceAtLeast(0)
    }

    private fun normalizeToHhMmSs(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.count { it == ':' } == 1) "$trimmed:00" else trimmed
    }

    private fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        if (totalItems <= 0) return 1
        return ((totalItems + pageSize - 1) / pageSize).coerceAtLeast(1)
    }

    companion object {
        private const val TAG = "ResultsViewModel"
    }
}
