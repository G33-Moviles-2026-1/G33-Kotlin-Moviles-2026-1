package com.example.andespace.ui.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.components.FriendCard
import com.example.andespace.ui.components.FriendStatusIcon
import com.example.andespace.ui.components.FriendsActionChip
import com.example.andespace.ui.components.MyStatusPickerDialog
import com.example.andespace.ui.components.friendStatusLabel
import com.example.andespace.ui.main.AssetIcon

@Composable
fun MyFriendsScreen(viewModel: FriendsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showStatusPicker) {
        MyStatusPickerDialog(
            selectedStatus = uiState.statusPickerSelection,
            isApplying = uiState.isApplyingStatus,
            onPrevious = { viewModel.cycleStatusPickerPrevious() },
            onNext = { viewModel.cycleStatusPickerNext() },
            onDismiss = { viewModel.dismissStatusPicker() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "My Friends",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FriendsActionChip(
                label = "My status:",
                trailing = {
                    FriendStatusIcon(
                        status = uiState.myStatus,
                        contentDescription = friendStatusLabel(uiState.myStatus),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { viewModel.showStatusPicker() },
                modifier = Modifier.weight(1f)
            )
            FriendsActionChip(
                label = "Add friends",
                trailing = {
                    AssetIcon(
                        assetPath = "icons/addfriend.svg",
                        contentDescription = "Add friends",
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { viewModel.openAddFriends() },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        when {
            uiState.isLoadingFriends && uiState.friendsList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.friendsList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You don't have any friends added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.friendsList, key = { it.email }) { friend ->
                        FriendCard(
                            friend = friend,
                            onDelete = { viewModel.removeFriend(friend.email) }
                        )
                    }
                }
            }
        }
    }
}
