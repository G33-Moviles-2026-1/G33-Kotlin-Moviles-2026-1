package com.example.andespace.ui.homepage

import com.example.andespace.data.location.GeoLocation

enum class ContentScreen {
    HOME,
    RESULTS,
    ROOM_DETAIL,
    MAKE_BOOKING
}

data class HomepageUiState(
    val contentScreen: ContentScreen = ContentScreen.HOME,
    val closeToMe: Boolean = false,
    val isLocating: Boolean = false,
    val locationError: Boolean = false,
    val userLocation: GeoLocation? = null
)
