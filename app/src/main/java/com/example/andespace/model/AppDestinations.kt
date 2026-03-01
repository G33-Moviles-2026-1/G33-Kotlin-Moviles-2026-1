package com.example.andespace.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    CLASSROOMS("Classrooms", Icons.Default.CoPresent),
    FAVORITES("Favorites", Icons.Default.FavoriteBorder),
    BOOKINGS("Bookings", Icons.Default.MeetingRoom),
    SCHEDULE("My Schedule", Icons.Default.EventAvailable)
}
