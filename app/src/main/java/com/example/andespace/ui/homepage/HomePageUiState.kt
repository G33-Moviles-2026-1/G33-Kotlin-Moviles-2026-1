package com.example.andespace.ui.homepage

enum class ContentScreen {
    HOME,
    RESULTS,
    ROOM_DETAIL,
    MAKE_BOOKING
}

data class HomepageUiState(
    val contentScreen: ContentScreen = ContentScreen.HOME
)
