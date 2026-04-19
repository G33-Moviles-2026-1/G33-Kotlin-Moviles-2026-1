package com.example.andespace.ui.schedule

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.RecommendedRoomOut
import com.example.andespace.ui.components.FeatureChip
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedRoomsScreen(
    viewModel: ScheduleViewModel,
    onBackClick: () -> Unit,
    onRoomClick: (RecommendedRoomOut) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.recommendationsData

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Recommended Rooms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (data == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            data.slots.forEach { slot ->
                items(slot.recommended_rooms) { room ->
                    RecommendedRoomCard(
                        room = room,
                        slotStart = slot.slot_start,
                        slotEnd = slot.slot_end,
                        onRoomClick = onRoomClick
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedRoomCard(
    room: RecommendedRoomOut,
    slotStart: String,
    slotEnd: String,
    onRoomClick: (RecommendedRoomOut) -> Unit
) {
    val rightBoxText = "Score: ${String.format(Locale.US, "%.1f", room.score)}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(18.dp))
            .clickable { onRoomClick(room) },
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.room_id,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = rightBoxText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${slotStart.take(5)} - ${slotEnd.take(5)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureChip(
                    text = "Cap: ${room.capacity}",
                    background = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.onSurface
                )

                val isVeryClose = room.reasons.near_previous_class || room.reasons.near_next_class

                if (isVeryClose) {
                    FeatureChip(text = "Very Close")
                }
                else {
                    val walkSeconds = listOfNotNull(room.to_next_seconds, room.from_previous_seconds)
                        .filter { it > 0f }
                        .minOrNull()

                    if (walkSeconds != null) {
                        val walkMinutes = ceil(walkSeconds / 60.0).toInt()
                        FeatureChip(text = "$walkMinutes min walk")
                    }
                }
            }
        }
    }
}
