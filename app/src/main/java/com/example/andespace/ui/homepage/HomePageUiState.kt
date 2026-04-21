package com.example.andespace.ui.homepage

import com.example.andespace.data.location.GeoLocation
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.RoomUtility

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
    val userLocation: GeoLocation? = null,
    val lastSearchConfig: HomeSearchConfig = HomeSearchConfig()
)

data class HomeSearchConfig(
    val classroom: String = "",
    val date: String? = null,
    val since: String? = null,
    val until: String? = null,
    val utilityDisplayNames: Set<String> = emptySet()
)

fun HomeSearchParams.toSearchConfig(): HomeSearchConfig {
    return HomeSearchConfig(
        classroom = classroom,
        date = date,
        since = since,
        until = until,
        utilityDisplayNames = utilities
            .map { RoomUtility.displayNameFromCode(it) }
            .toSet()
    )
}
