package com.example.andespace.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val assetIconPath: String? = null,
    val icon: ImageVector? = null
) {
    CLASSROOMS("Classrooms", assetIconPath = "icons/rooms.svg"),
    FAVORITES("Favorites", assetIconPath = "icons/favorites.svg"),
    BOOKINGS("Bookings", assetIconPath = "icons/bookings.svg"),
    SCHEDULE("My Schedule", assetIconPath = "icons/schedule.svg"),
    NAVIGATION("Navigation", assetIconPath = "icons/navigation.svg"),
    LOGIN("Log In", icon = Icons.Default.Lock),
    REGISTER("Register", icon = Icons.Default.PersonAdd),
    ACCOUNT_SETTINGS("Account Settings", icon = Icons.Default.Person),
    SETTINGS("Settings", icon = Icons.Default.Settings),
    FRIENDS("Friends", icon = Icons.Default.MonitorHeart)
}
