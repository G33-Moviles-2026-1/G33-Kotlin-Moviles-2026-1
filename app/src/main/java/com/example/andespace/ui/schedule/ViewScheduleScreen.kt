package com.example.andespace.ui.schedule
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.andespace.model.schedule.ScheduleClassOccurrenceOut
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ViewScheduleScreen(
    viewModel: ScheduleViewModel,
    onManuallyAddClick: () -> Unit,
    onDeleteScheduleClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilterDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateRangeText = uiState.scheduleData?.let { data ->
                        try {
                            val start = LocalDate.parse(data.week_start)
                            val end = LocalDate.parse(data.week_end)
                            val formatter = DateTimeFormatter.ofPattern("MMMM d", Locale.US)
                            "${start.format(formatter)} - ${end.format(formatter)}"
                        } catch (_: Exception) {
                            "Invalid Date Range"
                        }
                    } ?: "Loading..."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.loadPreviousWeek() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Week",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.
                                    clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { viewModel.loadNextWeek() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Week",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.
                                clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onManuallyAddClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manually Add Class",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(30.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(7.dp)
                                    )
                                .padding(2.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.resetToCurrentWeek() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Schedule",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(30.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(7.dp)
                                )
                                .padding(2.dp)
                        )
                    }
                    IconButton(onClick = onDeleteScheduleClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Schedule",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(30.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(7.dp)
                                )
                                .padding(2.dp)
                        )
                    }
                }
            }

            if (uiState.isLoading || uiState.errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(
                                text = uiState.errorMessage ?: "An unknown error occurred",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (!uiState.isLoading && uiState.errorMessage == null && uiState.scheduleData != null) {
                val scheduleData = uiState.scheduleData!!
                val groupedClasses = scheduleData.occurrences.groupBy { backendDay ->
                    backendDay.weekday.replaceFirstChar { it.uppercase() }
                }

                val weekStart = try {
                    LocalDate.parse(scheduleData.week_start)
                } catch (_: Exception) {
                    LocalDate.now()
                }

                for (i in 0..5) {
                    val currentDate = weekStart.plusDays(i.toLong())

                    val dayName = currentDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    val dayNumber = currentDate.dayOfMonth.toString()

                    val classesForDay = groupedClasses[dayName] ?: emptyList()

                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DayScheduleSection(
                                dayName = dayName,
                                dayNumber = dayNumber,
                                classes = classesForDay,
                                viewModel = viewModel,
                                isSelected = (selectedFilterDate == currentDate),
                                onDayClick = {
                                    selectedFilterDate = if (selectedFilterDate == currentDate) null else currentDate
                                }
                            )
                        }
                    }
                }
            }
        }

        if (selectedFilterDate != null) {
            ExtendedFloatingActionButton(
                onClick = {
                    val dateStr = selectedFilterDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    viewModel.loadRecommendations(dateStr)
                },
                icon = { Icon(Icons.Default.FilterList, contentDescription = "Filter") },
                text = { Text("Filter from schedule") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
@Composable
fun DayScheduleSection(
    dayName: String,
    dayNumber: String,
    classes: List<ScheduleClassOccurrenceOut>,
    viewModel: ScheduleViewModel,
    isSelected: Boolean,
    onDayClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                .clickable { onDayClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$dayName $dayNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }

        if (classes.isEmpty()) {
            Text(
                text = "No classes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
        } else {
            classes.forEach { classInfo ->
                ClassCard(
                    classInfo = classInfo,
                    onDeleteClick = { viewModel.deleteClass(classInfo.class_id) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ClassCard(
    classInfo: ScheduleClassOccurrenceOut,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = classInfo.start_time.take(5),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = classInfo.end_time.take(5),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classInfo.title ?: "Unknown Class",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = classInfo.room_id ?: classInfo.location_text ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete this class",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(30.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(2.dp)
                )
            }
        }
    }
}