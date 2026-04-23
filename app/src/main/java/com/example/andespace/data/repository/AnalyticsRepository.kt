package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.db.AnalyticsDao
import com.example.andespace.data.db.PendingAnalyticsEvent
import com.example.andespace.data.network.ApiService
import com.example.andespace.model.dto.AnalyticsEventRequest
import com.example.andespace.model.dto.RoomDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AnalyticsRepository(
    private val apiService: ApiService,
    private val analyticsDao: AnalyticsDao,
    private val gson: Gson
) {
    private val sessionId = UUID.randomUUID().toString()

    companion object {
        private const val ANALYTICS_QUEUE_TAG = "AnalyticsQueue"
        const val ANALYTICS_EVENT_FAVORITE_SUBMITTED = "favorite_submitted"
    }


    suspend fun logGenericEvent(event: AnalyticsEventRequest): Boolean {
        return try {
            val response = apiService.trackAnalyticsEvent(event)
            response.isSuccessful
        } catch (e: Exception) {
            saveEventOffline("GENERIC", gson.toJson(event))
            false
        }
    }

    private suspend fun saveEventOffline(type: String, jsonPayload: String) {
        withContext(Dispatchers.IO) {
            val eventEntity = PendingAnalyticsEvent(
                eventType = type,
                eventDataJson = jsonPayload,
                timestamp = System.currentTimeMillis()
            )
            analyticsDao.insertEvent(eventEntity)
            analyticsDao.pruneOldEvents()
            Log.w(ANALYTICS_QUEUE_TAG, "Offline: $type event queued for SyncManager.")
        }
    }

    suspend fun trackScreensTime(screenName: String) {
        val request = AnalyticsEventRequest(
            sessionId = sessionId,
            eventName = "open_screen_timestamp",
            screen = screenName
        )
        logGenericEvent(request)
    }

    suspend fun trackFavoriteEvent(room: RoomDto, added: Boolean) {
        val request = AnalyticsEventRequest(
            sessionId = sessionId,
            eventName = ANALYTICS_EVENT_FAVORITE_SUBMITTED,
            screen = "favorites",
            propsJson = mapOf(
                "room_id" to room.id,
                "room_name" to (room.name ?: ""),
                "building" to (room.building ?: ""),
                "building_code" to (room.buildingCode ?: ""),
                "action" to if (added) "add" else "remove"
            )
        )
        logGenericEvent(request)
    }

    suspend fun trackHomeEvent(eventName: String) {
        val request = AnalyticsEventRequest(
            sessionId = sessionId,
            eventName = eventName,
            screen = "home"
        )
        logGenericEvent(request)
    }

    suspend fun trackAppliedFilters(
        placeUsed: Boolean,
        timeUsed: Boolean,
        utilitiesUsed: Boolean,
        closeToMeUsed: Boolean
    ): Boolean {
        val request = AnalyticsEventRequest(
            sessionId = sessionId,
            eventName = "home_filters_opened",
            screen = "home",
            propsJson = mapOf(
                "place" to placeUsed,
                "time" to timeUsed,
                "utilities" to utilitiesUsed,
                "close_to_me" to closeToMeUsed
            )
        )
        return logGenericEvent(request)
    }
}