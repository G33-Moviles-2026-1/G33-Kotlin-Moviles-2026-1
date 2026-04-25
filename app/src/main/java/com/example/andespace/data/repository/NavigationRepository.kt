package com.example.andespace.data.repository

import android.content.Context
import com.example.andespace.data.location.LocationSensor
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.cache.RouteMemoryCache
import com.example.andespace.model.dto.NavigationNearestNodeResponse
import com.example.andespace.model.navigation.NavigationHistoryEntry
import com.example.andespace.model.navigation.NavigationRoute
import com.example.andespace.model.navigation.NavigationRouteResult
import com.example.andespace.model.navigation.NavigationSnapshot
import com.example.andespace.model.navigation.RouteKey
import com.example.andespace.model.dto.NavigationPathResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NavigationRepository(
    private val apiService: ApiService,
    context: Context,
    gson: Gson
) {
    private val memoryCache = RouteMemoryCache(maxSize = 10)
    private val persistenceDataSource = NavigationPersistenceDataSource(context, gson)
    private val stateMutex = Mutex()

    private var history: MutableList<NavigationHistoryEntry> = mutableListOf()
    private var historyIndex: Int = -1
    private var lastShownRouteKey: String? = null
    private var stateRestored = false

    suspend fun restoreNavigationState(): Result<NavigationRouteResult?> = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            restoreStateIfNeededLocked()
            Result.success(currentRouteResultLocked(fromCache = true, isUsingGpsOrigin = false))
        }
    }

    suspend fun getRoute(
        origin: String,
        destination: String,
        isUsingGpsOrigin: Boolean = false
    ): Result<NavigationRouteResult> = withContext(Dispatchers.IO) {
        try {
            stateMutex.withLock {
                restoreStateIfNeededLocked()

                val routeKey = RouteKey(origin = origin, destination = destination)
                val cacheKey = routeKey.cacheKey()
                val cachedRoute = memoryCache.get(cacheKey)
                if (cachedRoute != null) {
                    recordRouteVisitLocked(cacheKey)
                    persistStateLocked()
                    return@withLock Result.success(
                        buildRouteResultLocked(
                            route = cachedRoute,
                            fromCache = true,
                            isUsingGpsOrigin = isUsingGpsOrigin
                        )
                    )
                }

                val response = apiService.getNavigationPath(
                    fromRoom = origin,
                    toRoom = destination
                )

                if (!response.isSuccessful) {
                    val rawErrorBody = response.errorBody()?.string()
                    return@withLock Result.failure<NavigationRouteResult>(
                        Exception(extractErrorMessage(rawErrorBody, response.code()))
                    )
                }

                val responseBody = response.body() ?: throw Exception("Empty response body")
                val route = responseBody.toNavigationRoute(
                    fallbackOrigin = origin,
                    fallbackDestination = destination
                )

                memoryCache.put(cacheKey, route)
                recordRouteVisitLocked(cacheKey)
                persistStateLocked()

                Result.success(
                    buildRouteResultLocked(
                        route = route,
                        fromCache = false,
                        isUsingGpsOrigin = isUsingGpsOrigin
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getRouteFromHistoryBack(): Result<NavigationRouteResult?> = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            restoreStateIfNeededLocked()
            if (historyIndex <= 0) {
                return@withLock Result.success(null)
            }
            historyIndex -= 1
            val currentKey = history.getOrNull(historyIndex)?.cacheKey ?: lastShownRouteKey
            val route = currentKey?.let { memoryCache.get(it) } ?: return@withLock Result.success(null)
            lastShownRouteKey = currentKey
            persistStateLocked()
            Result.success(buildRouteResultLocked(route, fromCache = true, isUsingGpsOrigin = false))
        }
    }

    suspend fun getRouteFromHistoryForward(): Result<NavigationRouteResult?> = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            restoreStateIfNeededLocked()
            if (historyIndex >= history.lastIndex) {
                return@withLock Result.success(null)
            }
            historyIndex += 1
            val currentKey = history.getOrNull(historyIndex)?.cacheKey ?: lastShownRouteKey
            val route = currentKey?.let { memoryCache.get(it) } ?: return@withLock Result.success(null)
            lastShownRouteKey = currentKey
            persistStateLocked()
            Result.success(buildRouteResultLocked(route, fromCache = true, isUsingGpsOrigin = false))
        }
    }

    suspend fun resolveOriginFromGpsIfNeeded(locationSensor: LocationSensor): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val location = locationSensor.getCurrentLocation()
                    ?: return@withContext Result.failure(Exception("Could not get your current location."))

                val response = apiService.getNearestNavigationNode(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                if (!response.isSuccessful) {
                    val rawErrorBody = response.errorBody()?.string()
                    return@withContext Result.failure(Exception(extractErrorMessage(rawErrorBody, response.code())))
                }

                val nearestNode = response.body() ?: throw Exception("Empty response body")
                Result.success(nearestNode.buildingCode)
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "Could not determine your nearest location"))
            }
        }

    suspend fun persistState() {
        stateMutex.withLock {
            restoreStateIfNeededLocked()
            persistStateLocked()
        }
    }

    private suspend fun restoreStateIfNeededLocked() {
        if (stateRestored) return
        val snapshot = persistenceDataSource.readSnapshot() ?: run {
            stateRestored = true
            return
        }
        memoryCache.restoreFromSnapshot(snapshot.cachedRoutes)
        history = snapshot.history.toMutableList()
        historyIndex = when {
            history.isEmpty() -> -1
            snapshot.historyIndex < 0 -> history.lastIndex
            else -> snapshot.historyIndex.coerceIn(0, history.lastIndex)
        }
        lastShownRouteKey = snapshot.lastShownRouteKey ?: history.getOrNull(historyIndex)?.cacheKey
            ?: history.lastOrNull()?.cacheKey
        stateRestored = true
    }

    private fun recordRouteVisitLocked(cacheKey: String) {
        if (historyIndex < history.lastIndex) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        
        history.removeAll { it.cacheKey == cacheKey }

        history.add(
            NavigationHistoryEntry(
                cacheKey = cacheKey,
                visitedAt = System.currentTimeMillis()
            )
        )

        if (history.size > 10) {
            val keysToRemove = history.subList(0, history.size - 10).map { it.cacheKey }
            history = history.drop(history.size - 10).toMutableList()
            keysToRemove.forEach { key ->
                if (history.none { it.cacheKey == key }) {
                    memoryCache.evict(key)
                }
            }
        }

        historyIndex = history.lastIndex
        lastShownRouteKey = cacheKey
    }

    private fun currentRouteLocked(): NavigationRoute? {
        val currentKey = lastShownRouteKey
            ?: history.getOrNull(historyIndex)?.cacheKey
            ?: history.lastOrNull()?.cacheKey
        return currentKey?.let { memoryCache.peek(it) }
    }

    private fun currentRouteResultLocked(
        fromCache: Boolean,
        isUsingGpsOrigin: Boolean
    ): NavigationRouteResult? {
        val route = currentRouteLocked() ?: return null
        return buildRouteResultLocked(route, fromCache, isUsingGpsOrigin)
    }

    private fun buildRouteResultLocked(
        route: NavigationRoute,
        fromCache: Boolean,
        isUsingGpsOrigin: Boolean
    ): NavigationRouteResult {
        return NavigationRouteResult(
            route = route,
            fromCache = fromCache,
            canGoBack = historyIndex > 0,
            canGoForward = historyIndex in 0 until history.lastIndex,
            isUsingGpsOrigin = isUsingGpsOrigin
        )
    }

    private suspend fun persistStateLocked() {
        persistenceDataSource.persistSnapshot(
            memoryCache.toNavigationSnapshot(
                history = history.toList(),
                historyIndex = historyIndex,
                lastShownRouteKey = lastShownRouteKey
            )
        )
    }

    private fun NavigationPathResponse.toNavigationRoute(
        fallbackOrigin: String,
        fallbackDestination: String
    ): NavigationRoute {
        return NavigationRoute(
            origin = fromClassroom.takeIf { it.isNotBlank() } ?: fallbackOrigin,
            destination = toClassroom.takeIf { it.isNotBlank() } ?: fallbackDestination,
            steps = steps,
            totalTimeSeconds = totalTime
        )
    }
}
