package com.example.andespace.model.cache

import com.example.andespace.model.navigation.CachedRouteEntry
import com.example.andespace.model.navigation.NavigationRoute
import com.example.andespace.model.navigation.NavigationSnapshot
import java.util.LinkedHashMap

class RouteMemoryCache(
    private val maxSize: Int = 10
) {
    private val routes = LinkedHashMap<String, CachedRouteEntry>(maxSize, 0.75f, true)

    fun get(cacheKey: String): NavigationRoute? {
        val cachedEntry = routes[cacheKey] ?: return null
        routes[cacheKey] = cachedEntry.copy(lastAccessedAt = System.currentTimeMillis())
        return routes[cacheKey]?.route
    }

    fun peek(cacheKey: String): NavigationRoute? {
        return routes[cacheKey]?.route
    }

    fun put(cacheKey: String, route: NavigationRoute) {
        routes[cacheKey] = CachedRouteEntry(
            cacheKey = cacheKey,
            route = route,
            lastAccessedAt = System.currentTimeMillis()
        )
        trimToSize()
    }

    fun contains(cacheKey: String): Boolean = routes.containsKey(cacheKey)

    fun evict(cacheKey: String): Boolean = routes.remove(cacheKey) != null

    fun clear() {
        routes.clear()
    }

    fun toSnapshot(): List<CachedRouteEntry> = routes.values.toList()

    fun restoreFromSnapshot(entries: List<CachedRouteEntry>) {
        routes.clear()
        entries.forEach { entry ->
            routes[entry.cacheKey] = entry
        }
        trimToSize()
    }

    fun toNavigationSnapshot(
        history: List<com.example.andespace.model.navigation.NavigationHistoryEntry>,
        historyIndex: Int,
        lastShownRouteKey: String?
    ): NavigationSnapshot {
        return NavigationSnapshot(
            cachedRoutes = toSnapshot(),
            history = history,
            historyIndex = historyIndex,
            lastShownRouteKey = lastShownRouteKey
        )
    }

    private fun trimToSize() {
        while (routes.size > maxSize) {
            val eldestKey = routes.entries.iterator().next().key
            routes.remove(eldestKey)
        }
    }
}