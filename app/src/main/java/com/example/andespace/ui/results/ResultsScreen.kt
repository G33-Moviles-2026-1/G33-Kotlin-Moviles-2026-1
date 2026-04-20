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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.components.RoomCard
import com.example.andespace.ui.components.PaginationFooter


@Composable
fun ResultsScreen(
    rooms: List<RoomDto>,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    hasUploadedSchedule: Boolean,
    errorMessage: String?,
    currentPage: Int,
    totalPages: Int,
    showingCachedResults: Boolean = false,
    favoriteIds: Set<String> = emptySet(),
    onFavoriteClick: ((RoomDto) -> Unit)? = null,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

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
                                showScheduleLabel = isUserLoggedIn && hasUploadedSchedule,
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
                onPrevPage = onPrevPage,
                onNextPage = onNextPage
            )
        }
    }
}


