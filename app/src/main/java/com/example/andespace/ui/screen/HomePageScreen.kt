@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.andespace.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.andespace.AssetIcon
import com.example.andespace.ui.theme.PrimaryYellow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val UTILITIES_OPTIONS = listOf(
    "Blackout",
    "Power Outlet",
    "Television",
    "Interactive Classroom",
    "Mobile WhiteBoards",
    "Computer Classroom"
)

@Composable
fun HomePageScreen(
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        UtilitiesFilterSheet(
            onDismiss = { showFilterSheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Where do you\nwant to go?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(18.dp))

            SearchCard(
                onFilterClick = { showFilterSheet = true },
                onSearchClick = onSearchClick
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour, minute)

private val dateDisplayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun formatDateMillis(millis: Long): String =
    dateDisplayFormat.format(millis)

@Composable
private fun SearchCard(
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    var classroomInput by remember { mutableStateOf("") }
    val initialDateMillis = remember { System.currentTimeMillis() }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var closeToMe by remember { mutableStateOf(true) }
    var sinceHour by remember { mutableStateOf(8) }
    var sinceMinute by remember { mutableStateOf(0) }
    var untilHour by remember { mutableStateOf(18) }
    var untilMinute by remember { mutableStateOf(0) }
    var showSincePicker by remember { mutableStateOf(false) }
    var showUntilPicker by remember { mutableStateOf(false) }

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = classroomInput,
                        onValueChange = { classroomInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (classroomInput.isEmpty()) {
                                    Text(
                                        text = "Classroom ej. ML 201, ML 5, ML",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                IconButton(onClick = onFilterClick) {
                    AssetIcon(
                        assetPath = "icons/filters.svg",
                        contentDescription = "Filters",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssetIcon(
                    assetPath = "icons/location.svg",
                    contentDescription = "Location",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Close to me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(
                    checked = closeToMe,
                    onCheckedChange = { closeToMe = it }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelLarge
                )
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

@Composable
private fun UtilitiesFilterSheet(onDismiss: () -> Unit) {
    var selectedOptions by remember {
        mutableStateOf(UTILITIES_OPTIONS.toSet())
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Close"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Utilities",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                UTILITIES_OPTIONS.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = option in selectedOptions,
                            onCheckedChange = {
                                selectedOptions = if (it) selectedOptions + option
                                else selectedOptions - option
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

