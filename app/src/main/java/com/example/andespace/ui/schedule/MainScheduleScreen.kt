package com.example.andespace.ui.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainScheduleScreen(
    scheduleViewModel: ScheduleViewModel
) {
    val uiState by scheduleViewModel.uiState.collectAsState()

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

            uiState.hasSchedule -> {
                ViewScheduleScreen(viewModel = scheduleViewModel)
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