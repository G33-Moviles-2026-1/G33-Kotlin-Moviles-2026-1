package com.example.andespace.ui.recommendations

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.components.TimeSlotSelector

@Composable
fun RecommendationsScreen(
    viewModel: RecommendationsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            if (uiState.isSearchActive) {
                IconButton(
                    onClick = { viewModel.resetSearch() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Text(
                text = "Auto Search ✨",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Crossfade(targetState = uiState.isSearchActive, label = "SearchState") { isActive ->
                if (!isActive) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TimeSlotSelector(
                            selectedDate = uiState.selectedDate,
                            selectedStartTime = uiState.selectedStartTime,
                            onDateChange = viewModel::updateDateSelection,
                            onTimeChange = viewModel::updateTimeSelection
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { viewModel.startAutoSearch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = uiState.selectedStartTime != null
                        ) {
                            Text("Find the perfect room")
                        }
                    }
                } else {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.currentRoom != null) {
                        RoomRecommendationCard(
                            room = uiState.currentRoom!!,
                            onSkip = { viewModel.onInteract(InteractionAction.SKIP) },
                            onFavorite = { viewModel.onInteract(InteractionAction.FAVORITE) },
                            onBook = { viewModel.onInteract(InteractionAction.BOOK) }
                        )
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Green
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("You've seen all recommendations!")
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.resetSearch() }) {
                                Text("Search Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomRecommendationCard(
    room: RoomSearchItemOut,
    onSkip: () -> Unit,
    onFavorite: () -> Unit,
    onBook: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Recommended for you", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = room.room_id, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                Text(text = room.building_name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Capacity: ${room.capacity} people", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(onClick = onSkip, containerColor = MaterialTheme.colorScheme.errorContainer) {
                Icon(Icons.Default.Close, contentDescription = "Skip")
            }

            FloatingActionButton(onClick = onFavorite, containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            }

            ExtendedFloatingActionButton(
                onClick = onBook,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Book")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Book Now")
            }
        }
    }
}