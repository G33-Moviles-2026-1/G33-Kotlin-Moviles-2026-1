package com.example.andespace.ui.friends

import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.WeeklyScheduleOut
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class FriendsUiState(
    val isLoadingFriends: Boolean = false,
    val friendsList: List<FriendItemOut> = emptyList(),
    val friendsError: String? = null,

    val searchEmail: String = "",
    val incomingRequests: List<FriendItemOut> = emptyList(),
    val isLoadingRequests: Boolean = false,

    val selectedFriend: FriendItemOut? = null,
    val isLoadingSchedule: Boolean = false,
    val friendScheduleData: WeeklyScheduleOut? = null,
    val scheduleError: String? = null,
    val currentWeekDate: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
)