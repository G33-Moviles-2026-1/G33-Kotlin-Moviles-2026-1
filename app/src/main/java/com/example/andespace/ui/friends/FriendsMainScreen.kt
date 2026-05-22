package com.example.andespace.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun FriendsMainScreen(viewModel: FriendsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.selectedFriend != null) {
        FriendsScheduleScreen(
            viewModel = viewModel,
            onBackClick = { viewModel.clearSelectedFriend() }
        )
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.contentScreen) {
                FriendsContentScreen.MY_FRIENDS -> MyFriendsScreen(viewModel = viewModel)
                FriendsContentScreen.ADD_FRIENDS -> AddFriendsScreen(viewModel = viewModel)
            }
        }
    }
}
