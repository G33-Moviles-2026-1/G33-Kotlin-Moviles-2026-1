@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.andespace.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.andespace.AssetIcon
import com.example.andespace.model.dto.AvailabilitySlotDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.RoomTimeWindowDto
import com.example.andespace.model.dto.RoomWeeklyAvailabilityDto
import com.example.andespace.ui.detailRoom.DetailRoomUiState
import com.example.andespace.ui.theme.PrimaryYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoadMakeBookingScreen(
    detailRoomUiState: DetailRoomUiState,
    bookingsUiState: BookingsUIState,
    onDateChange: (String) -> Unit,
    onCreateBooking: (CreateBookingRequest) -> Unit,
    onBookingCreatedConsumed: () -> Unit,
    onBookingCreatedNavigate: () -> Unit
) {
    val room = detailRoomUiState.room
    val roomId = room?.id ?: ""
    val date = detailRoomUiState.selectedDate ?: ""
    val windows = room?.availableSlots.orEmpty()

    if (bookingsUiState.bookingCreatedSuccess) {
        LaunchedEffect(Unit) {
            onBookingCreatedConsumed()
            onBookingCreatedNavigate()
        }
    }

    LoadMakeBookingContent(
        roomId = roomId,
        selectedDate = date,
        availableWindows = windows,
        isLoadingSlots = detailRoomUiState.isLoadingAvailability,
        isCreating = bookingsUiState.isCreating,
        errorMessage = bookingsUiState.createError,
        onDateChanged = onDateChange,
        onBook = onCreateBooking
    )
}

private fun formatSlotLabel(window: RoomWeeklyAvailabilityDto): String {
    val start = window.start?.take(5) ?: "?"
    val end = window.end?.take(5) ?: "?"
    return "$start - $end"
}

private fun parseApiDateToMillis(dateValue: String): Long {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateValue)?.time
            ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun millisToApiDate(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
}

@Composable
private fun LoadMakeBookingContent(
    roomId: String,
    selectedDate: String,
    availableWindows: List<RoomWeeklyAvailabilityDto>,
    isLoadingSlots: Boolean,
    isCreating: Boolean,
    errorMessage: String?,
    onDateChanged: (String) -> Unit,
    onBook: (CreateBookingRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedSlotIndex by remember(availableWindows) { mutableIntStateOf(0) }
    var showTimeDropdown by remember { mutableStateOf(false) }

    var purpose by remember { mutableStateOf("study_alone") }
    var peopleCount by remember { mutableStateOf("") }

    val purposes = listOf(
        "study_alone" to "Study Alone",
        "study_small_group" to "Study Small Group",
        "chill_alone" to "Chill Alone",
        "hangout_friends" to "Hangout Friends",
        "tutoring_big_group" to "Tutoring Big Group"
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseApiDateToMillis(selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChanged(millisToApiDate(it))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("OK") }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Book $roomId",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pick a time",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .height(40.dp)
                    .padding(horizontal = 12.dp)
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssetIcon(
                    assetPath = "icons/schedule.svg",
                    contentDescription = "Calendar",
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(40.dp)
                        .padding(horizontal = 12.dp)
                        .clickable(enabled = availableWindows.isNotEmpty() && !isLoadingSlots) {
                            showTimeDropdown = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when {
                            isLoadingSlots -> "Loading..."
                            availableWindows.isEmpty() -> "No slots"
                            else -> formatSlotLabel(availableWindows[selectedSlotIndex])
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (availableWindows.isEmpty() && !isLoadingSlots)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onBackground
                    )
                    AssetIcon(
                        assetPath = "icons/clock.svg",
                        contentDescription = "Time",
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showTimeDropdown,
                    onDismissRequest = { showTimeDropdown = false }
                ) {
                    availableWindows.forEachIndexed { index, window ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = formatSlotLabel(window),
                                    fontWeight = if (index == selectedSlotIndex)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedSlotIndex = index
                                showTimeDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select the purpose",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        purposes.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { purpose = value }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = purpose == value,
                    onClick = { purpose = value },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Write how many people",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = peopleCount,
            onValueChange = { value ->
                if (value.all { it.isDigit() } && value.length <= 2) {
                    peopleCount = value
                }
            },
            placeholder = { Text("Min. 1 - Max. 30") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val canBook = availableWindows.isNotEmpty() && !isCreating && !isLoadingSlots

        Button(
            onClick = {
                val window = availableWindows[selectedSlotIndex]
                onBook(
                    CreateBookingRequest(
                        roomId = roomId,
                        date = selectedDate,
                        startTime = window.start ?: return@Button,
                        endTime = window.end ?: return@Button,
                        purpose = purpose
                    )
                )
            },
            enabled = canBook,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(58.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryYellow,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
