package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RoomAvailabilityResponse(
    @SerializedName("room_id") val roomId: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("weekday") val weekday: String? = null,
    @SerializedName("available_slots") val availableSlots: List<AvailabilitySlotDto> = emptyList(),
    @SerializedName("blocked_slots") val blockedSlots: List<AvailabilitySlotDto> = emptyList(),
    @SerializedName(value = "matching_windows", alternate = ["available_windows", "windows"])
    val matchingWindows: List<RoomTimeWindowDto> = emptyList(),
    @SerializedName("weekly_availability")
    val weeklyAvailability: List<RoomWeeklyAvailabilityDto> = emptyList(),
    @SerializedName("available_since") val availableSince: String? = null,
    @SerializedName("available_until") val availableUntil: String? = null
)

data class AvailabilitySlotDto(
    @SerializedName(value = "start", alternate = ["since", "from", "start_time"])
    val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"])
    val end: String? = null,
    @SerializedName("is_available") val isAvailable: Boolean? = null
)

fun RoomAvailabilityResponse.toTimeWindows(dateValue: String): List<RoomTimeWindowDto> {
    val fromAvailableSlots = availableSlots
        .asSequence()
        .filter { it.isAvailable != false }
        .mapNotNull { slot ->
            val start = slot.start
            val end = slot.end
            if (start.isNullOrBlank() || end.isNullOrBlank()) {
                null
            } else {
                RoomTimeWindowDto(start = start, end = end)
            }
        }
        .toList()
    if (fromAvailableSlots.isNotEmpty()) return fromAvailableSlots

    if (matchingWindows.isNotEmpty()) return matchingWindows

    val weeklyWindows = windowsForDateFromWeeklyAvailability(dateValue)
    if (weeklyWindows.isNotEmpty()) return weeklyWindows

    val since = availableSince
    val until = availableUntil
    if (!since.isNullOrBlank() && !until.isNullOrBlank()) {
        return listOf(RoomTimeWindowDto(start = since, end = until))
    }
    return emptyList()
}

private fun RoomAvailabilityResponse.windowsForDateFromWeeklyAvailability(
    dateValue: String
): List<RoomTimeWindowDto> {
    val selectedDate = parseApiDate(dateValue) ?: return emptyList()
    val dayKey = dayNameEnglish(selectedDate)

    return weeklyAvailability
        .asSequence()
        .filter { slot ->
            normalizeKey(slot.day.orEmpty()) == normalizeKey(dayKey) && slot.isValidOn(selectedDate)
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

private fun normalizeKey(value: String): String {
    val noAccents = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return noAccents.trim().lowercase(Locale.ROOT)
}