package com.example.andespace.ui.schedule

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.andespace.model.schedule.ScheduleClassOccurrenceOut
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.andespace.R
import com.example.andespace.ui.components.CustomIconButton
import com.example.andespace.ui.components.IconPosition

@Composable
fun ViewScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadIcsFile(context, uri, onSuccess = {
                viewModel.loadSchedule()
            })
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.scheduleData?.let { data ->
                        val startMonthDay = data.week_start.substringAfter("-")
                        val endMonthDay = data.week_end.substringAfter("-")

                        Text(
                            text = "$startMonthDay to $endMonthDay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                    CustomIconButton(
                        text = "Change",
                        onClick = { filePickerLauncher.launch("*/*") },
                        iconResId = R.drawable.ic_file,
                        iconPosition = IconPosition.END,
                        textPosition = Alignment.CenterStart
                    ,
                    )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (uiState.scheduleData != null) {

                val groupedClasses = uiState.scheduleData!!.occurrences.groupBy { backendDay ->
                    backendDay.weekday.replaceFirstChar { it.uppercase() }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    weekDays.forEach { dayName ->
                        val classesForDay = groupedClasses[dayName] ?: emptyList()

                        item {
                            DayScheduleSection(dayName = dayName, classes = classesForDay)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DayScheduleSection(dayName: String, classes: List<ScheduleClassOccurrenceOut>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (classes.isEmpty()) {
            Text(
                text = "No classes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        } else {
            classes.forEach { classInfo ->
                ClassCard(classInfo)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ClassCard(classInfo: ScheduleClassOccurrenceOut) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    text = classInfo.room_id ?: "TBD",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}