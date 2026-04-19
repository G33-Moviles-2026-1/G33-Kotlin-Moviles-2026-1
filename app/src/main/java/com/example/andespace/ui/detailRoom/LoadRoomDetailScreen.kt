package com.example.andespace.ui.detailRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.andespace.model.RoomUtility
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.theme.PrimaryYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadRoomDetailScreen(
    room: RoomDto?,
    selectedDate: String?,
    isLoadingAvailability: Boolean,
    availabilityError: String?,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onDateChange: (String) -> Unit,
    onBookRoom: () -> Unit = {},
) {
    if (room == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("No room selected")
        }
        return
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDateValue = selectedDate ?: currentDateApiValue()

    val displayDate = formatDisplayDate(selectedDateValue)
    val dayName = formatDisplayDay(selectedDateValue)
    val windows = room.matchingWindows
        .mapNotNull { window ->
            val start = window.start ?: return@mapNotNull null
            val end = window.end ?: return@mapNotNull null
            "$start - $end"
        }
        .ifEmpty {
            listOfNotNull(
                room.availableSince?.let { since ->
                    val until = room.availableUntil ?: ""
                    "$since - $until".trim()
                }
            )
        }

    val utilities = room.utilities
        .map { RoomUtility.displayNameFromCode(it) }
        .ifEmpty {
            listOf("Blackout", "Power Outlet", "Mobile Whiteboards")
        }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseApiDateToMillis(selectedDateValue)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val newDate =
                            datePickerState.selectedDateMillis?.let { millisToApiDate(it) }
                                ?: selectedDateValue
                        onDateChange(newDate)
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = room.id,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Building: ${room.building ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Capacity: ${room.capacity ?: 0} people",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (onFavoriteClick != null) {
                    SmallIconButton(
                        isFavorite = isFavorite,
                        onClick = onFavoriteClick
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            Color.Black,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Selected date",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Availability",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6A6A6A)
                )
            }
        }

        item {
            Text(
                text = "Available",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Color(0xFFE1E1E1),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isLoadingAvailability -> {
                        Text(
                            text = "Loading availability...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    !availabilityError.isNullOrBlank() -> {
                        Text(
                            text = availabilityError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB3261E),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    windows.isEmpty() -> {
                        Text(
                            text = "No available time slots for this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    else -> {
                        windows.forEach { windowText ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        Color.Black,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = windowText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Utilities",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                utilities.forEach { utility ->
                    UtilityChip(text = utility)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onBookRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Book Room",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SmallIconButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun UtilityChip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDisplayDate(dateValue: String?): String {
    if (dateValue.isNullOrBlank()) {
        return SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date())
    }
    return try {
        val source = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = source.parse(dateValue)
        if (date != null) {
            SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(date)
        } else {
            dateValue
        }
    } catch (_: Exception) {
        dateValue
    }
}

private fun formatDisplayDay(dateValue: String?): String {
    if (dateValue.isNullOrBlank()) return "Monday"
    return try {
        val source = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = source.parse(dateValue)
        if (date != null) {
            SimpleDateFormat("EEEE", Locale.ENGLISH).format(date)
        } else {
            "Monday"
        }
    } catch (_: Exception) {
        "Monday"
    }
}

private fun currentDateApiValue(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

private fun parseApiDateToMillis(dateValue: String): Long {
    return try {
        val source = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        source.parse(dateValue)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun millisToApiDate(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
}
