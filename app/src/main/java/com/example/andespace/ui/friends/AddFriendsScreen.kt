package com.example.andespace.ui.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.components.CustomYellowButton
import com.example.andespace.ui.components.FriendPendingRequestRow
import com.example.andespace.ui.components.FriendSuggestionRow
import com.example.andespace.ui.components.FriendsSectionBox
import com.example.andespace.ui.main.AssetIcon

@Composable
fun AddFriendsScreen(viewModel: FriendsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.openMyFriends() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to My Friends")
            }
            Text(
                text = "Add friends",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by username") },
            leadingIcon = {
                AssetIcon(
                    assetPath = "icons/searchusername.svg",
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomYellowButton(
            text = "Send friend request",
            onClick = { viewModel.sendFriendRequest() },
            enabled = uiState.searchQuery.isNotBlank()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pending requests:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FriendsSectionBox {
            if (uiState.isLoadingRequests) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else if (uiState.incomingRequests.isEmpty() && uiState.outgoingRequests.isEmpty()) {
                Text(
                    text = "No pending requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.incomingRequests.forEach { request ->
                        FriendPendingRequestRow(
                            name = request.displayName,
                            isOutgoing = false,
                            onDecline = { viewModel.declineFriendRequest(request.email) },
                            onAccept = { viewModel.acceptFriendRequest(request.email) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.outgoingRequests.forEach { request ->
                        FriendPendingRequestRow(
                            name = request.username.replaceFirstChar { it.titlecase() },
                            isOutgoing = true,
                            onCancel = { viewModel.cancelOutgoingRequest(request.email) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "People you might know:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FriendsSectionBox {
            if (uiState.isLoadingSuggestions) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else if (uiState.suggestions.isEmpty()) {
                Text(
                    text = "No suggestions right now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.suggestions.forEach { username ->
                        FriendSuggestionRow(
                            name = username.replaceFirstChar { it.titlecase() },
                            onAdd = { viewModel.sendSuggestionRequest(username) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
