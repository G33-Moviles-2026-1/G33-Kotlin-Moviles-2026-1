@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.andespace.ui.homepage

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.andespace.ui.main.AssetIcon
import com.example.andespace.data.location.GeoLocation
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.RoomUtility
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.components.CustomYellowButton
import com.example.andespace.ui.results.ResultsScreen
import com.example.andespace.ui.theme.PrimaryYellow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

@Composable
fun LoadHomePageScreen(
    contentScreen: ContentScreen,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    hasUploadedSchedule: Boolean,
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    searchError: String?,
    rooms: List<RoomDto>,
    currentPage: Int,
    totalPages: Int,
    showingCachedResults: Boolean = false,
    favoriteIds: Set<String> = emptySet(),
    onFavoriteClick: ((RoomDto) -> Unit)? = null,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (contentScreen) {
        ContentScreen.HOME -> LoadHomeSearchScreen(
            modifier = modifier,
            isSearching = isSearching,
            searchError = searchError,
            onSearchClick = onSearchClick,
            closeToMe = closeToMe,
            isLocating = isLocating,
            locationError = locationError,
            userLocation = userLocation,
            onRequestCurrentLocation = onRequestCurrentLocation,
            onLocationPermissionDenied = onLocationPermissionDenied,
            onCloseToMeDisabled = onCloseToMeDisabled,
            onClearLocationError = onClearLocationError,
            onFiltersOpened = onFiltersOpened
        )

        ContentScreen.RESULTS -> ResultsScreen(
            rooms = rooms,
            isSearching = isSearching,
            errorMessage = searchError,
            currentPage = currentPage,
            totalPages = totalPages,
            favoriteIds = favoriteIds,
            onFavoriteClick = onFavoriteClick,
            onRoomClick = onRoomClick,
            onPrevPage = onPrevPage,
            onNextPage = onNextPage,
            modifier = modifier
        )

        ContentScreen.ROOM_DETAIL,
        ContentScreen.MAKE_BOOKING -> Unit
    }
}

@Composable
private fun LoadHomeSearchScreen(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    searchError: String? = null,
    onSearchClick: (HomeSearchParams) -> Unit = {},
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
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
            closeToMe = closeToMe,
            isLocating = isLocating,
            locationError = locationError,
            userLocation = userLocation,
            onRequestCurrentLocation = onRequestCurrentLocation,
            onLocationPermissionDenied = onLocationPermissionDenied,
            onCloseToMeDisabled = onCloseToMeDisabled,
            onClearLocationError = onClearLocationError,
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

/** Material3 usa la fecha local a medianoche en UTC para cada día del calendario. */
private fun formatDateMillis(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

private fun nextOpenDate(start: LocalDate): LocalDate {
    return if (start.dayOfWeek == DayOfWeek.SUNDAY) start.plusDays(1) else start
}

@Composable
private fun SearchCard(
    selectedUtilities: Set<String>,
    isSearching: Boolean = false,
    searchError: String? = null,
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onFilterClick: () -> Unit,
    onSearchClick: (HomeSearchParams) -> Unit,
) {
    val context = LocalContext.current
    var classroomInput by remember { mutableStateOf("") }
    val initialDateMillis = remember {
        nextOpenDate(LocalDate.now(ZoneId.systemDefault()))
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onRequestCurrentLocation()
        } else {
            onLocationPermissionDenied()
        }
    }

    var sinceHour by remember { mutableStateOf(8) }
    var sinceMinute by remember { mutableStateOf(0) }
    var untilHour by remember { mutableStateOf(18) }
    var untilMinute by remember { mutableStateOf(0) }
    var sinceSet by remember { mutableStateOf(false) }
    var untilSet by remember { mutableStateOf(false) }
    var missingTimeError by remember { mutableStateOf<String?>(null) }
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
                sinceSet = true
                missingTimeError = null
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
                untilSet = true
                missingTimeError = null
                showUntilPicker = false
            }
        )
    }
    if (showDatePicker) {
        val (firstSelectable, lastSelectable) = remember(showDatePicker) {
            val start = nextOpenDate(LocalDate.now(ZoneId.systemDefault()))
            start to start.plusDays(7)
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val picked = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    return !picked.isBefore(firstSelectable) &&
                        !picked.isAfter(lastSelectable) &&
                        picked.dayOfWeek != DayOfWeek.SUNDAY
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year in firstSelectable.year..lastSelectable.year
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
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = classroomInput,
                    onValueChange = { if (it.length <= 15) classroomInput = it },
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
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                timeText = if (sinceSet) formatTime(sinceHour, sinceMinute) else "--:--",
                onClick = { showSincePicker = true },
                modifier = Modifier.weight(1f)
            )
            TimePickerPill(
                label = "Until",
                timeText = if (untilSet) formatTime(untilHour, untilMinute) else "--:--",
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
                onCheckedChange = { checked ->
                    onClearLocationError()
                    if (checked) {
                        val alreadyGranted =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (alreadyGranted) {
                            onRequestCurrentLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    } else {
                        onCloseToMeDisabled()
                    }
                }
            )
        }

        if (isLocating) {
            Text(
                text = "Getting your location...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (locationError) {
            Text(
                text = "Could not get your location. Enable GPS and try again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        val displayedError = missingTimeError ?: searchError
        if (displayedError != null) {
            Text(
                text = displayedError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        val isSearchBlockedByLocation = closeToMe && isLocating
        CustomYellowButton(
            text = when {
                isSearching -> "Searching..."
                isSearchBlockedByLocation -> "Getting location..."
                else -> "Search"
            },
            enabled = !isSearching && !isSearchBlockedByLocation,
            onClick = {
                if (!isSearching) {
                    if (!sinceSet || !untilSet) {
                        missingTimeError = "You must select both times (Since and Until) to search."
                        return@CustomYellowButton
                    }
                    missingTimeError = null
                    val params = HomeSearchParams(
                        classroom = classroomInput,
                        date = formatDateMillis(selectedDateMillis),
                        since = if (sinceSet) formatTime(sinceHour, sinceMinute) else null,
                        until = if (untilSet) formatTime(untilHour, untilMinute) else null,
                        closeToMe = closeToMe,
                        utilities = selectedUtilities.mapNotNull { RoomUtility.codeFromDisplayName(it) },
                        userLatitude = if (closeToMe) userLocation?.latitude else null,
                        userLongitude = if (closeToMe) userLocation?.longitude else null
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
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
            color = MaterialTheme.colorScheme.outline
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                RoomUtility.displayNames.forEach { option ->
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

