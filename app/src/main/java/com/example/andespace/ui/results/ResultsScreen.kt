package com.example.andespace.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.common.UserMessages
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.components.RoomCard
import com.example.andespace.ui.components.PaginationFooter
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.homepage.HomepageViewModel


@Composable
fun ResultsScreen(
    modifier: Modifier = Modifier,
    resultsViewModel: ResultsViewModel,
    favoritesViewModel: FavoritesViewModel,
    detailRoomViewModel: DetailRoomViewModel,
    homepageViewModel: HomepageViewModel,
    onRequireLogin: () -> Unit,
    isUserLoggedIn: Boolean
) {
    val listState = rememberLazyListState()
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    val favoritesUiState by favoritesViewModel.uiState.collectAsState()
    val favoriteIds = favoritesUiState.favoriteIds


    val currentPage = resultsUiState.currentPage
    val rooms = resultsUiState.rooms
    val totalPages = resultsUiState.totalPages
    val isSearching = resultsUiState.isSearching
    val showOfflinePlaceholder = resultsUiState.showOfflinePlaceholder
    val showingCachedResults = resultsUiState.showingCachedResults

    val onFavoriteClick: (RoomDto) -> Unit = { room ->
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
            if (showingCachedResults) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Showing cached results",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            when {
                showOfflinePlaceholder -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No connection",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = UserMessages.RESULTS_NO_CONNECTION,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
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
                                onFavoriteClick = onFavoriteClick.let { { it(room) } },
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


