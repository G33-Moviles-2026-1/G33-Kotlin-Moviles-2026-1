package com.example.andespace.ui.recommendations

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
import com.example.andespace.model.dto.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    viewModel: RecommendationsViewModel,
    onBackClick: () -> Unit
) {
    val purposes = remember {
        listOf(
            "study_alone" to "Study Alone",
            "study_small_group" to "Study Small Group",
            "chill_alone" to "Chill Alone",
            "hangout_friends" to "Hangout Friends",
            "tutoring_big_group" to "Tutoring Big Group"
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showBookingDialog && uiState.currentRoom != null) {
        val currentRoomDto = uiState.currentRoom!!.toRoomDto()
        var expanded by remember { mutableStateOf(false) }

        val selectedDisplay = purposes.find { it.first == uiState.bookingPurpose }?.second ?: ""

        AlertDialog(
            onDismissRequest = { viewModel.closeBookingDialog() },
            title = { Text("Quick Book: ${currentRoomDto.id}") },
            text = {
                Column {
                    Text("Time: ${uiState.selectedStartTime} - ${uiState.selectedEndTime}")
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDisplay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Meeting Purpose") },
                            placeholder = { Text("Select a purpose") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            purposes.forEach { (key, display) ->
                                DropdownMenuItem(
                                    text = { Text(display) },
                                    onClick = {
                                        viewModel.updateBookingPurpose(key)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmQuickBooking(currentRoomDto) },
                    enabled = uiState.bookingPurpose.isNotBlank() && !uiState.isBookingInProgress
                ) {
                    if (uiState.isBookingInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Confirm")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeBookingDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Auto Search",
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
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.currentRoom != null) {
                val currentRoomOut = uiState.currentRoom!!
                val currentRoomDto = currentRoomOut.toRoomDto()
                val isFavorite = currentRoomOut.room_id in uiState.favoriteIds
                RoomRecommendationCard(
                    room = currentRoomDto,
                    isFavorite = isFavorite,
                    onSkip = { viewModel.onInteract(InteractionAction.SKIP) },
                    onFavorite = { viewModel.toggleFavorite(currentRoomDto, isFavorite) },
                    onBook = { viewModel.openBookingDialog() }
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
                    Text("You have seen all recommendations for this time slot.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
fun RoomRecommendationCard(
    room: RoomDto,
    isFavorite: Boolean,
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
                Text(
                    text = "Recommended for you",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = room.id,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                (room.building ?: room.buildingCode)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Capacity: ${room.capacity} people",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = onSkip,
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Default.Close, contentDescription = "Skip")
            }

            // Dynamically change the icon based on local favorite state
            FloatingActionButton(
                onClick = onFavorite,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                )
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

fun RoomSearchItemOut.toRoomDto(): RoomDto {
    return RoomDto(
        id = this.room_id,
        name = this.room_number,
        building = this.building_name,
        capacity = this.capacity,
        buildingCode = this.building_code,
        utilities = this.utilities,
    )
}