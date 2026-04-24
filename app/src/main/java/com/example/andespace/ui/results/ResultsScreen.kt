package com.example.andespace.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.components.RoomCard
import com.example.andespace.ui.components.PaginationFooter
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.homepage.HomepageViewModel


@Composable
fun ResultsScreen(
    modifier: Modifier = Modifier,
    favoriteIds: Set<String> = emptySet(),
    resultsViewModel: ResultsViewModel,
    favoritesViewModel: FavoritesViewModel,
    detailRoomViewModel: DetailRoomViewModel,
    homepageViewModel: HomepageViewModel,
    onRequireLogin: () -> Unit,
    isUserLoggedIn: Boolean
) {
    val listState = rememberLazyListState()
    val resultsUiState by resultsViewModel.uiState.collectAsState()


    val currentPage = resultsUiState.currentPage
    val errorMessage = resultsUiState.errorMessage
    val rooms = resultsUiState.rooms
    val totalPages = resultsUiState.totalPages
    val isSearching = resultsUiState.isSearching

    fun onFavoriteClick (room: RoomDto) {
        if (isUserLoggedIn) {
            favoritesViewModel.toggleFavorite(room)
        } else {
            onRequireLogin()
        }
    }

    fun onRoomClick(room: RoomDto){
        resultsViewModel.onRoomClick(room)
        detailRoomViewModel.setRoom(
            room = room,
            selectedDate = resultsUiState.selectedSearchDate
        )
        homepageViewModel.onShowRoomDetailScreen()
    }

    LaunchedEffect(currentPage) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (errorMessage != null && rooms.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when {
                errorMessage != null && rooms.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(rooms) { index, room ->
                            RoomCard(
                                room = room,
                                cardIndex = index,
                                isFavorite = room.id in favoriteIds,
                                onFavoriteClick = onFavoriteClick?.let { { it(room) } },
                                onClick = { onRoomClick(room) }
                            )
                        }
                    }
                }
            }


            PaginationFooter(
                currentPage = currentPage,
                totalPages = totalPages,
                isSearching = isSearching,
                onPrevPage = {resultsViewModel.onPreviousPage()},
                onNextPage = {resultsViewModel.onNextPage()}
            )
        }
    }
}


