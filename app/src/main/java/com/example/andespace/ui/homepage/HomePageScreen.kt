@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.andespace.ui.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import com.example.andespace.AssetIcon
import com.example.andespace.data.model.HomeSearchParams
import androidx.compose.ui.unit.sp
import com.example.andespace.ui.components.CustomYellowButton
import com.example.andespace.ui.results.ResultsScreen
import com.example.andespace.ui.theme.PrimaryYellow
import com.example.andespace.data.model.dto.RoomDto
import java.text.SimpleDateFormat
import java.util.Locale

private val UTILITIES_OPTIONS = mapOf(
    "Blackout" to "blackout",
    "Power Outlet" to "power_outlet",
    "Television" to "television",
    "Interactive Classroom" to "interactive_classroom",
    "Mobile WhiteBoards" to "mobile_whiteboards     ",
    "Computer" to "computer",
    "Videobeam" to "videobeam"
)

@Composable
fun HomepageContent(
    contentScreen: ContentScreen,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    searchError: String?,
    rooms: List<RoomDto>,
    currentPage: Int,
    totalPages: Int,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (contentScreen) {
        ContentScreen.HOME -> HomePageScreen(
            modifier = modifier,
            isSearching = isSearching,
            searchError = searchError,
            onSearchClick = onSearchClick,
            onFiltersOpened = onFiltersOpened
        )

        ContentScreen.RESULTS -> ResultsScreen(
            rooms = rooms,
            isSearching = isSearching,
            isUserLoggedIn = isUserLoggedIn,
            errorMessage = searchError,
            currentPage = currentPage,
            totalPages = totalPages,
            onRoomClick = onRoomClick,
            onPrevPage = onPrevPage,
            onNextPage = onNextPage,
            modifier = modifier
        )

        ContentScreen.ROOM_DETAIL -> Unit
        else -> {}
    }
}

@Composable
fun HomePageScreen(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    searchError: String? = null,
    onSearchClick: (HomeSearchParams) -> Unit = {},
    onFiltersOpened: () -> Unit = {},
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedUtilities by remember { mutableStateOf(emptySet<String>()) }
    if (showFilterSheet) {
        UtilitiesFilterSheet(
            selectedOptions = selectedUtilities,
            onSelectedOptionsChange = { selectedUtilities = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Where do you\nwant to go?",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 28.sp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(18.dp))

        SearchCard(
            selectedUtilities = selectedUtilities,
            isSearching = isSearching,
            searchError = searchError,
            onFilterClick = {
                onFiltersOpened()
                showFilterSheet = true
            },
            onSearchClick = onSearchClick
        )
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour, minute)

private val dateDisplayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun formatDateMillis(millis: Long): String =
    dateDisplayFormat.format(millis)

@Composable
private fun SearchCard(
    selectedUtilities: Set<String>,
    isSearching: Boolean = false,
    searchError: String? = null,
    onFilterClick: () -> Unit,
    onSearchClick: (HomeSearchParams) -> Unit,
) {
    var classroomInput by remember { mutableStateOf("") }
    val initialDateMillis = remember { System.currentTimeMillis() }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var closeToMe by remember { mutableStateOf(false) }
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
        val todayMillis = remember {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        val maxDateMillis = remember { todayMillis + 7L * 24 * 60 * 60 * 1000 }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in todayMillis..maxDateMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    return year == currentYear
                }
            }
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

    Column(
        modifier = Modifier.fillMaxWidth()
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AssetIcon(
                    assetPath = "icons/location.svg",
                    contentDescription = "Location",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Close to me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Checkbox(
                    checked = closeToMe,
                    onCheckedChange = { closeToMe = it }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (searchError != null) {
                Text(
                    text = searchError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            CustomYellowButton(
                text = if (isSearching) "Searching..." else "Search",
                onClick = {
                    if (!isSearching) {
                        val params = HomeSearchParams(
                            classroom = classroomInput,
                            date = formatDateMillis(selectedDateMillis),
                            since = formatTime(sinceHour, sinceMinute),
                            until = formatTime(untilHour, untilMinute),
                            closeToMe = closeToMe,
                            utilities = selectedUtilities.mapNotNull { UTILITIES_OPTIONS[it] }
                        )
                        onSearchClick(params)
                    }
                }
            )
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
private fun UtilitiesFilterSheet(
    selectedOptions: Set<String>,
    onSelectedOptionsChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var localSelected by remember(selectedOptions) { mutableStateOf(selectedOptions) }
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
                UTILITIES_OPTIONS.keys.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = option in localSelected,
                            onCheckedChange = {
                                localSelected = if (it) localSelected + option
                                else localSelected - option
                                onSelectedOptionsChange(localSelected)
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

