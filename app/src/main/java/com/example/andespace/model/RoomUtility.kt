package com.example.andespace.model

import java.util.Locale

enum class RoomUtility(
    val code: String,
    val displayName: String
) {
    BLACKOUT("blackout", "Blackout"),
    POWER_OUTLET("power_outlet", "Power Outlet"),
    TELEVISION("television", "Television"),
    INTERACTIVE_CLASSROOM("interactive_classroom", "Interactive Classroom"),
    MOBILE_WHITEBOARDS("mobile_whiteboards", "Mobile Whiteboards"),
    COMPUTER("computer", "Computer"),
    VIDEOBEAM("videobeam", "Videobeam");

    companion object {
        val displayNames: List<String> = entries.map { it.displayName }

        fun codeFromDisplayName(displayName: String): String? {
            return entries.firstOrNull { it.displayName == displayName }?.code
        }

        fun displayNameFromCode(rawCode: String): String {
            val normalized = rawCode.trim().lowercase(Locale.ROOT)
            val known = entries.firstOrNull { it.code == normalized }
            if (known != null) return known.displayName

            return normalized
                .replace('_', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { token ->
                    token.replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.ROOT) else ch.toString()
                    }
                }
                .ifBlank { rawCode }
        }
    }
}
