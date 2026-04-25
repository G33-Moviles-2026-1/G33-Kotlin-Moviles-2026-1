package com.example.andespace.model.navigation

import java.util.Locale

data class RouteKey(
    val origin: String,
    val destination: String
) {
    fun cacheKey(): String = buildCacheKey(origin, destination)

    companion object {
        fun buildCacheKey(origin: String, destination: String): String {
            return "${normalize(origin)}|${normalize(destination)}"
        }

        private fun normalize(value: String): String {
            return value.trim()
                .replace(Regex("\\s+"), " ")
                .uppercase(Locale.US)
        }
    }
}

data class NavigationRoute(
    val origin: String,
    val destination: String,
    val steps: List<String>,
    val totalTimeSeconds: Int
)

data class CachedRouteEntry(
    val cacheKey: String,
    val route: NavigationRoute,
    val lastAccessedAt: Long
)

data class NavigationHistoryEntry(
    val cacheKey: String,
    val visitedAt: Long
)

data class NavigationSnapshot(
    val cachedRoutes: List<CachedRouteEntry> = emptyList(),
    val history: List<NavigationHistoryEntry> = emptyList(),
    val historyIndex: Int = -1,
    val lastShownRouteKey: String? = null
)

data class NavigationRouteResult(
    val route: NavigationRoute,
    val fromCache: Boolean,
    val canGoBack: Boolean,
    val canGoForward: Boolean,
    val isUsingGpsOrigin: Boolean = false
)