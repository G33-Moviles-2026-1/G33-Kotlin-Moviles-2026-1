package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.model.dto.AnalyticsEventRequest
import com.example.andespace.model.dto.RoomGapSearchAnalyticsRequest

object AnalyticsEventQueue {

    private const val TAG = "AnalyticsQueue"

    sealed class PendingEvent {
        data class Generic(val request: AnalyticsEventRequest) : PendingEvent()
        data class RoomGapSearch(val request: RoomGapSearchAnalyticsRequest) : PendingEvent()
    }

    private val queue = ArrayDeque<PendingEvent>()
    private val lock = Any()

    val isEmpty: Boolean get() = synchronized(lock) { queue.isEmpty() }

    fun enqueue(event: PendingEvent) = synchronized(lock) {
        queue.add(event)
        val label = when (event) {
            is PendingEvent.Generic -> event.request.eventName
            is PendingEvent.RoomGapSearch -> "room_gap_search"
        }
        Log.d(TAG, "ENCOLADO (sin enviar al servidor) event=$label, pendientes=${queue.size}")
    }

    fun drainAll(): List<PendingEvent> = synchronized(lock) {
        val snapshot = queue.toList()
        queue.clear()
        snapshot
    }
}
