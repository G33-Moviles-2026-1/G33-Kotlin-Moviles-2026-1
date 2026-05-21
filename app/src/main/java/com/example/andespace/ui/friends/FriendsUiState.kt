package com.example.andespace.ui.friends

import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.model.dto.WeeklyScheduleOut
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class FriendsContentScreen {
    MY_FRIENDS,
    ADD_FRIENDS
}

data class OutgoingFriendRequest(
    val email: String,
    val username: String
)

data class FriendsUiState(
    val contentScreen: FriendsContentScreen = FriendsContentScreen.MY_FRIENDS,
    val myStatus: UserStatus = UserStatus.FREE,
    val showStatusPicker: Boolean = false,

    val isLoadingFriends: Boolean = false,
    val friendsList: List<FriendItemOut> = emptyList(),
    val friendsError: String? = null,

    val searchQuery: String = "",
    val isLoadingSuggestions: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val incomingRequests: List<FriendItemOut> = emptyList(),
    val outgoingRequests: List<OutgoingFriendRequest> = emptyList(),
    val isLoadingRequests: Boolean = false,

    val selectedFriend: FriendItemOut? = null,
    val isLoadingSchedule: Boolean = false,
    val friendScheduleData: WeeklyScheduleOut? = null,
    val scheduleError: String? = null,
    val currentWeekDate: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
)
