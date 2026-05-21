package com.example.andespace.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.ScheduleClassOccurrenceOut
import com.example.andespace.ui.components.NoConnectionPlaceholder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun FriendsScheduleScreen(
    viewModel: FriendsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val friendName = uiState.selectedFriend?.username ?: "Friend"

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "$friendName's Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateRangeText = try {
                        val start = uiState.currentWeekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        val end = start.plusDays(6)
                        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
                        "${start.format(formatter)} - ${end.format(formatter)}"
                    } catch (_: Exception) {
                        "Schedule"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.loadPreviousWeek() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Week",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.loadNextWeek() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Week",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            when {
                uiState.isLoadingSchedule -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                uiState.friendScheduleData == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            NoConnectionPlaceholder()
                        }
                    }
                }

                else -> {
                    val scheduleData = uiState.friendScheduleData!!
                    val groupedClasses = scheduleData.occurrences.groupBy { backendDay ->
                        backendDay.weekday.replaceFirstChar { it.uppercase() }
                    }

                    val weekStart = try {
                        LocalDate.parse(scheduleData.week_start)
                    } catch (_: Exception) {
                        uiState.currentWeekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    }

                    for (i in 0..5) {
                        val currentDate = weekStart.plusDays(i.toLong())
                        val dayName = currentDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                        val dayNumber = currentDate.dayOfMonth.toString()
                        val classesForDay = groupedClasses[dayName] ?: emptyList()

                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                ReadOnlyDayScheduleSection(
                                    dayName = dayName,
                                    dayNumber = dayNumber,
                                    classes = classesForDay
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadOnlyDayScheduleSection(
    dayName: String,
    dayNumber: String,
    classes: List<ScheduleClassOccurrenceOut>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$dayName $dayNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        if (classes.isEmpty()) {
            Text(
                text = "No classes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        } else {
            classes.forEach { classInfo ->
                ReadOnlyClassCard(classInfo = classInfo)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReadOnlyClassCard(
    classInfo: ScheduleClassOccurrenceOut
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
        }
    }
}