package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RoomSearchResponse(
    @SerializedName(value = "items", alternate = ["rooms"]) val rooms: List<RoomDto> = emptyList(),
    @SerializedName("total") val total: Int? = null
)

data class RoomDto(
    @SerializedName(value = "room_id", alternate = ["id"]) val id: String,
    @SerializedName(value = "room_number", alternate = ["name"]) val name: String? = null,
    @SerializedName(value = "building_name", alternate = ["building"]) val building: String? = null,
    @SerializedName("building_code") val buildingCode: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("reliability") val reliability: Double? = null,
    @SerializedName("distance_meters") val distanceMeters: Double? = null,
    @SerializedName("availability_status") val availabilityStatus: String? = null,
    @SerializedName("available_since") val availableSince: String? = null,
    @SerializedName("available_until") val availableUntil: String? = null,
    @SerializedName("wait_seconds") val waitSeconds: Int? = null,
    @SerializedName("matching_windows") val matchingWindows: List<RoomTimeWindowDto> = emptyList(),
    @SerializedName("weekly_availability") val weeklyAvailability: List<RoomWeeklyAvailabilityDto> = emptyList(),
    @SerializedName("utilities") val utilities: List<String> = emptyList()
)

data class RoomTimeWindowDto(
    @SerializedName(value = "start", alternate = ["since", "from", "start_time"])
    val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"])
    val end: String? = null
)

data class RoomWeeklyAvailabilityDto(
    @SerializedName("day") val day: String? = null,
    @SerializedName(value = "start", alternate = ["since", "from", "start_time"])
    val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"])
    val end: String? = null,
    @SerializedName("valid_from") val validFrom: String? = null,
    @SerializedName("valid_to") val validTo: String? = null
)

fun RoomDto.windowsForDate(dateValue: String): List<RoomTimeWindowDto> {
    val selectedDate = parseApiDate(dateValue) ?: return matchingWindows

    val dayKey = dayNameEnglish(selectedDate)
    val weeklyWindows = weeklyAvailability
        .asSequence()
        .filter { slot ->
            val slotDay = slot.day?.trim()?.lowercase(Locale.ROOT)
            slotDay == dayKey && slot.isValidOn(selectedDate)
        }
        .mapNotNull { slot ->
            val start = slot.start
            val end = slot.end
            if (start.isNullOrBlank() || end.isNullOrBlank()) {
                null
            } else {
                RoomTimeWindowDto(start = start, end = end)
            }
        }
        .sortedBy { it.start ?: "" }
        .toList()

    if (weeklyWindows.isNotEmpty()) return weeklyWindows
    return matchingWindows
}

private fun RoomWeeklyAvailabilityDto.isValidOn(selectedDate: Date): Boolean {
    val fromDate = validFrom?.let { parseApiDate(it) }
    val toDate = validTo?.let { parseApiDate(it) }

    if (fromDate != null && selectedDate.before(fromDate)) return false
    if (toDate != null && selectedDate.after(toDate)) return false
    return true
}

private fun parseApiDate(value: String): Date? {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }.parse(value)
    }.getOrNull()
}

private fun dayNameEnglish(date: Date): String {
    return SimpleDateFormat("EEEE", Locale.ENGLISH)
        .format(date)
        .lowercase(Locale.ROOT)
}
