package com.example.andespace.ui.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.andespace.model.dto.RecommendedRoomOut

@Composable
fun MainScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    onNavigateToRoomDetail: (RecommendedRoomOut) -> Unit
) {
    val uiState by scheduleViewModel.uiState.collectAsState()

    if (uiState.classIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleViewModel.cancelDeleteClass() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant ,
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete this class? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { scheduleViewModel.confirmDeleteClass() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) { Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer) }
            },
            dismissButton = {
                TextButton(
                    onClick = { scheduleViewModel.cancelDeleteClass() },
                    colors =  ButtonDefaults.buttonColors(containerColor =  MaterialTheme.colorScheme.surfaceVariant )
                ) {
                    Text("Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
               }
            }
        )
    }

    if (uiState.showDeleteScheduleConfirm) {
        AlertDialog(
            onDismissRequest = { scheduleViewModel.cancelDeleteSchedule() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to permanently delete your entire schedule? This will wipe all classes.") },
            confirmButton = {
                Button(
                    onClick = { scheduleViewModel.confirmDeleteSchedule() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) { Text("Delete All", color = MaterialTheme.colorScheme.onErrorContainer) }
            },
            dismissButton = {
                TextButton(
                    onClick = { scheduleViewModel.cancelDeleteSchedule()},
                    colors =  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            uiState.isAddingManualClass -> {
                AddClassScreen(
                    viewModel = scheduleViewModel,
                    onBackClick = {
                        scheduleViewModel.hideAddClassScreen()
                    },
                    onClassAdded = {
                        scheduleViewModel.hideAddClassScreen()
                        scheduleViewModel.loadSchedule()
                    }
                )
            }

            uiState.isShowingRecommendations -> {
                RecommendedRoomsScreen(
                    viewModel = scheduleViewModel,
                    onBackClick = { scheduleViewModel.hideRecommendations()},
                    onRoomClick =  onNavigateToRoomDetail
                )
            }

            uiState.hasSchedule -> {
                ViewScheduleScreen(viewModel = scheduleViewModel,
                    onManuallyAddClick = {
                        scheduleViewModel.showAddClassScreen()
                    },
                    onDeleteScheduleClick = {scheduleViewModel.promptDeleteSchedule()}
                )
            }

            else -> {
                LoadScheduleScreen(
                    viewModel = scheduleViewModel,
                    onScheduleLoaded = {
                        scheduleViewModel.loadSchedule()
                    } ,
                    onLoadManuallyClick = {
                        scheduleViewModel.showAddClassScreen()
                    }
                )
            }
        }
    }
}