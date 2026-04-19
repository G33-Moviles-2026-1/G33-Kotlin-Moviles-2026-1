package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.model.dto.AnalyticsEventRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsRepository(private val apiService: ApiService) {
    private val sessionId = AnalyticsSessionManager.currentSessionId
    companion object {
        private const val ANALYTICS_QUEUE_TAG = "AnalyticsQueue"
    }

    suspend fun flushPendingAnalytics() {
        val events = AnalyticsEventQueue.drainAll()
        if (events.isEmpty()) {
            Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: cola vacía, nada que enviar")
            return
        }
        Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: intentando enviar ${events.size} evento(s) pendiente(s)")
        val failed = mutableListOf<AnalyticsEventQueue.PendingEvent>()
        for (event in events) {
            try {
                when (event) {
                    is AnalyticsEventQueue.PendingEvent.Generic -> {
                        val r = apiService.trackAnalyticsEvent(event.request)
                        if (!r.isSuccessful) failed.add(event)
                    }

                    is AnalyticsEventQueue.PendingEvent.RoomGapSearch -> {
                        val r = apiService.trackRoomGapSearch(event.request)
                        if (!r.isSuccessful) failed.add(event)
                    }
                }
            } catch (_: Exception) {
                failed.add(event)
            }
        }
        val ok = events.size - failed.size
        if (ok > 0) Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: enviados OK al backend: $ok")
        if (failed.isNotEmpty()) {
            Log.w(ANALYTICS_QUEUE_TAG, "FLUSH: fallaron ${failed.size}, se vuelven a encolar")
            failed.forEach { AnalyticsEventQueue.enqueue(it) }
        }
    }

    suspend fun trackScreensTime(screenName: String) {
        withContext(Dispatchers.IO) {
            val request = AnalyticsEventRequest(
                sessionId = sessionId,
                eventName = "open_screen_timestamp",
                screen = screenName
            )
            try {
                apiService.trackAnalyticsEvent(request)
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
            }
        }
    }
}