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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.andespace.AssetIcon
import com.example.andespace.model.Booking
import com.example.andespace.ui.theme.PrimaryYellow
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour, minute)

private val dateDisplayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun formatDateMillis(millis: Long): String =
    dateDisplayFormat.format(millis)

@Composable
fun EditBookingScreen(
    booking: Booking,
    onSave: (Booking) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDateMillis by remember { mutableLongStateOf(booking.dateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    var sinceHour by remember { mutableIntStateOf(booking.sinceHour) }
    var sinceMinute by remember { mutableIntStateOf(booking.sinceMinute) }
    var showSincePicker by remember { mutableStateOf(false) }

    var untilHour by remember { mutableIntStateOf(booking.untilHour) }
    var untilMinute by remember { mutableIntStateOf(booking.untilMinute) }
    var showUntilPicker by remember { mutableStateOf(false) }

    var purpose by remember { mutableStateOf(booking.purpose) }
    var peopleCount by remember {
        mutableStateOf(if (booking.peopleCount > 0) booking.peopleCount.toString() else "")
    }

    val purposes = listOf("Studying", "Tutoring", "Chilling")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
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

    if (showSincePicker) {
        TimePickerDialog(
            initialHour = sinceHour,
            initialMinute = sinceMinute,
            onDismiss = { showSincePicker = false },
            onConfirm = { h, m ->
                sinceHour = h
                sinceMinute = m
                showSincePicker = false
            }
        )
    }
    if (showUntilPicker) {
        TimePickerDialog(
            initialHour = untilHour,
            initialMinute = untilMinute,
            onDismiss = { showUntilPicker = false },
            onConfirm = { h, m ->
                untilHour = h
                untilMinute = m
                showUntilPicker = false
            }
        )
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
            text = "Book ${booking.roomName}",
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
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(10.dp))
                .background(Color.White)
                .height(40.dp)
                .padding(horizontal = 12.dp)
                .clickable(onClick = { showDatePicker = true }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formatDateMillis(selectedDateMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            AssetIcon(
                assetPath = "icons/schedule.svg",
                contentDescription = "Calendar",
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimePickerPill(
                label = "Since",
                timeText = formatTime(sinceHour, sinceMinute),
                onClick = { showSincePicker = true },
                modifier = Modifier.weight(1f)
            )
            TimePickerPill(
                label = "Until",
                timeText = formatTime(untilHour, untilMinute),
                onClick = { showUntilPicker = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select the purpose",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        purposes.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { purpose = option }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = purpose == option,
                    onClick = { purpose = option },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Black,
                        unselectedColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = option, fontSize = 15.sp, color = Color.Black)
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
            placeholder = { Text("Min. 1 – Max. 30") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val count = peopleCount.toIntOrNull() ?: 1
                    onSave(
                        booking.copy(
                            dateMillis = selectedDateMillis,
                            sinceHour = sinceHour,
                            sinceMinute = sinceMinute,
                            untilHour = untilHour,
                            untilMinute = untilMinute,
                            purpose = purpose,
                            peopleCount = count.coerceIn(1, 30)
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimePickerPill(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(10.dp))
            .background(Color.White)
            .height(40.dp)
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label $timeText",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier
                .height(18.dp)
                .width(1.dp),
            color = Color(0xFFE8E8E8)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AssetIcon(
            assetPath = "icons/clock.svg",
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onConfirm(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryYellow,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
