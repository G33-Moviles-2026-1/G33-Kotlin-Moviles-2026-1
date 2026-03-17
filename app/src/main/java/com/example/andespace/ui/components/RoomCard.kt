package com.example.andespace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.andespace.data.model.dto.RoomDto
import com.example.andespace.ui.theme.LightYellow

@Composable
fun RoomCard(room: RoomDto, cardIndex: Int, onClick: () -> Unit = {}) {
    val availability = room.availabilityStatus
        ?: if (cardIndex % 2 == 0) "available_after" else "free_in_schedule"
    val isFreeNow = availability.equals("free_in_schedule", ignoreCase = true)
    val headerColor = if (isFreeNow) Color(0xFFD9E8D9) else Color(0xFFEFE5D4)
    val statusColor = if (isFreeNow) Color(0xFF4C9654) else Color(0xFFD8A327)
    val statusTitle = if (isFreeNow) "FREE IN YOUR SCHEDULE" else "AVAILABLE AFTER"
    val scheduleText = buildString {
        val since = room.availableSince ?: "15:20:00"
        val until = room.availableUntil ?: "16:50:00"
        append("From ")
        append(since)
        append(" to ")
        append(until)
    }
    val extraUtilities = room.utilities
    val roomId = room.id
    val roomName = room.name ?: "Unnamed"
    val buildingText = room.building ?: "Unnamed"
    val waitSeconds = room.waitSeconds ?: (16 + (cardIndex * 20))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .border(1.dp, Color.Black,RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = roomId,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$waitSeconds Seconds",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bldg. $buildingText • Room $roomName",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFreeNow) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = "Availability status",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = statusTitle,
                        color = statusColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scheduleText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureChip(
                    text = "Cap: ${room.capacity ?: 0}",
                    background = LightYellow,
                    borderColor = Color(0xFF8F8A30)
                )
                extraUtilities.take(2).forEach { utility ->
                    FeatureChip(text = utility)
                }
            }
        }
    }
}