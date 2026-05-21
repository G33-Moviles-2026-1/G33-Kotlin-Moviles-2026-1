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
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.InteractionAction
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.RoomSearchItemOut
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.ui.components.NoConnectionPlaceholder

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
    val isOnline by NetworkMonitor.isOnline.collectAsState()

    if (uiState.showBookingDialog && uiState.currentRoom != null) {
        val currentRoomDto = uiState.currentRoom!!.toRoomDto()
        var expanded by remember { mutableStateOf(false) }

        val selectedDisplay = purposes.find { it.first == uiState.bookingPurpose }?.second ?: ""

        AlertDialog(
            onDismissRequest = { viewModel.closeBookingDialog() },
            title = { Text("Quick Book: ${currentRoomDto.id}") },
            text = {
                Column {
                    Text("Select Time Slot", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    var expandedTime by remember { mutableStateOf(false) }
                    val matchingWindows = uiState.currentRoom?.matching_windows ?: emptyList()

                    val selectedSlotDisplay = uiState.selectedBookingSlot?.let {
                        "${it.start.substringBeforeLast(":")} - ${it.end.substringBeforeLast(":")}"
                    } ?: "No slots available"

                    ExposedDropdownMenuBox(
                        expanded = expandedTime,
                        onExpandedChange = { expandedTime = !expandedTime }
                    ) {
                        OutlinedTextField(
                            value = selectedSlotDisplay,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTime) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTime,
                            onDismissRequest = { expandedTime = false }
                        ) {
                            if (matchingWindows.isEmpty()) {
                                DropdownMenuItem(text = { Text("No valid slots") }, onClick = {})
                            }
                            matchingWindows.forEach { window ->
                                val displayTime = "${window.start.substringBeforeLast(":")} - ${window.end.substringBeforeLast(":")}"
                                DropdownMenuItem(
                                    text = { Text(displayTime) },
                                    onClick = {
                                        viewModel.updateBookingSlot(window)
                                        expandedTime = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Select Purpose", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDisplay,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Meeting Purpose") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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

                    if (uiState.bookingError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.bookingError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
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
            if (!isOnline) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    NoConnectionPlaceholder()
                }
            }
            else if (uiState.isLoading && uiState.recommendations.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (uiState.currentRoom != null) {
                val currentRoomOut = uiState.currentRoom!!
                val currentRoomDto = currentRoomOut.toRoomDto()
                val isFavorite = currentRoomOut.room_id in uiState.favoriteIds
                RoomRecommendationCard(
                    room = currentRoomDto,
                    isFavorite = isFavorite,
                    onSkip = {
                        if (isFavorite) {
                            viewModel.nextRoom()
                        } else {
                            viewModel.onInteract(InteractionAction.SKIP)
                        }
                    },
                    onFavorite = { viewModel.toggleFavorite(currentRoomDto, isFavorite) },
                    onBook = { viewModel.openBookingDialog() }
                )
            }
            else if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else {
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
                    Text("We don't have more recommendations for you. Try again Tomorrow.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
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

