package com.example.andespace.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val assetIconPath: String,
) {
    CLASSROOMS("Classrooms", "icons/rooms.svg"),
    FAVORITES("Favorites", "icons/favorites.svg"),
    BOOKINGS("Bookings", "icons/bookings.svg"),
    SCHEDULE("My Schedule", "icons/schedule.svg"),
    HISTORY("History", "icons/history.svg")
    LOGIN("Log In", Icons.Default.Lock),
    REGISTER("Register", Icons.Default.PersonAdd),
    HOME("Home", Icons.Default.PersonAdd)
}
