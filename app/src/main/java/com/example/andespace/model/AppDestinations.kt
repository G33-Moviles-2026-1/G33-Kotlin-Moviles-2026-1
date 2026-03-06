package com.example.andespace.model

enum class AppDestinations(
    val label: String,
    val assetIconPath: String,
) {
    CLASSROOMS("Classrooms", "icons/rooms.svg"),
    FAVORITES("Favorites", "icons/favorites.svg"),
    BOOKINGS("Bookings", "icons/bookings.svg"),
    SCHEDULE("My Schedule", "icons/schedule.svg")
}
