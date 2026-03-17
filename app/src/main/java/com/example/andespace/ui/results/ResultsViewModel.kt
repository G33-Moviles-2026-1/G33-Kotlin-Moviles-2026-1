package com.example.andespace.ui.results

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.model.HomeSearchParams
import com.example.andespace.data.model.dto.RoomDto
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

    fun onSearchClick(params: HomeSearchParams) {
        lastSearchParams = params
        requestSearchPage(params = params, page = 1, trackEvent = true)
    }

    fun onRoomClick(room: RoomDto) {
        _uiState.update {
            it.copy(selectedRoom = room)
        }
    }

    fun onNextPage() {
        val state = _uiState.value
        if (state.isSearching) return

        val params = lastSearchParams ?: return
        val nextPage = (state.currentPage + 1).coerceAtMost(state.totalPages)
        if (nextPage == state.currentPage) return

        requestSearchPage(params = params, page = nextPage)
    }

    fun onPreviousPage() {
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
            }

            repository.searchRooms(params, limit = pageSize, offset = offset)
                .fold(
                    onSuccess = { response ->
                        val totalItems = response.total ?: (offset + response.rooms.size)
                        val pages = calculateTotalPages(totalItems = totalItems, pageSize = pageSize)

                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                rooms = response.rooms,
                                selectedSearchDate = params.date,
                                currentPage = page,
                                totalPages = pages,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                errorMessage = error.message ?: "Search error"
                            )
                        }
                    }
                )
        }
    }

    private fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        if (totalItems <= 0) return 1
        return ((totalItems + pageSize - 1) / pageSize).coerceAtLeast(1)
    }

    companion object {
        private const val TAG = "ResultsViewModel"
    }
}
