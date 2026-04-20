package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.db.AnalyticsDao
import com.example.andespace.data.db.SyncActionDao
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.model.dto.AnalyticsEventRequest
import com.example.andespace.model.dto.ManualClassIn
import com.example.andespace.model.dto.RoomGapSearchAnalyticsRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class SyncManager(
    private val syncDao: SyncActionDao,
    private val analyticsDao: AnalyticsDao,
    private val scheduleRepository: ScheduleRepository,
    private val apiService: ApiService,
    private val gson: Gson
) {
    private val syncMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    try {
                        flushQueue()
                    } catch (e: Exception) {
                        Log.e("SyncManager", "Critical failure during flush. Surviving.", e)
                    }
                }
            }
        }
    }

    private suspend fun flushQueue() {
        syncMutex.withLock {
            val pendingAnalytics = analyticsDao.getAllPendingEvents()
            if (pendingAnalytics.isNotEmpty()) {
                Log.d("SyncManager", "Flushing ${pendingAnalytics.size} Analytics events...")
                val successfulIds = mutableListOf<Int>()

                for (event in pendingAnalytics) {
                    try {
                        if (event.eventType == "GENERIC") {
                            val request = gson.fromJson(event.eventDataJson, AnalyticsEventRequest::class.java)
                            apiService.trackAnalyticsEvent(request)
                        } else if (event.eventType == "ROOM_GAP") {
                            val request = gson.fromJson(event.eventDataJson, RoomGapSearchAnalyticsRequest::class.java)
                            apiService.trackRoomGapSearch(request)
                        }
                        successfulIds.add(event.id)
                    } catch (e: IOException) {
                        Log.w("SyncManager", "Network dropped while sending analytics.")
                        break
                    } catch (e: Exception) {
                        Log.e("SyncManager", "Poison pill analytics event dropped: ${event.id}")
                        successfulIds.add(event.id)
                    }
                }
                if (successfulIds.isNotEmpty()) {
                    analyticsDao.deleteEvents(successfulIds)
                }
            }

            val pendingActions = syncDao.getAllPendingActions()
            if (pendingActions.isEmpty()) return@withLock

            Log.d("SyncManager", "Flushing ${pendingActions.size} Schedule actions...")
            var networkFailed = false

            for (action in pendingActions) {
                try {
                    when (action.actionType) {
                        "ADD_CLASS" -> {
                            val newClass = gson.fromJson(action.payload, ManualClassIn::class.java)
                            scheduleRepository.syncManualClassWithBackend(newClass)
                        }
                        "DELETE_CLASS" -> {
                            scheduleRepository.syncDeleteClassWithBackend(action.payload)
                        }
                        "DELETE_SCHEDULE" -> {
                            scheduleRepository.syncDeleteScheduleWithBackend()
                        }
                    }
                    syncDao.deleteAction(action.id)
                } catch (e: IOException) {
                    networkFailed = true
                    break
                } catch (e: Exception) {
                    syncDao.deleteAction(action.id)
                }
            }

            if (!networkFailed) {
                scheduleRepository.syncEntireScheduleFromBackend()
            }
        }
    }
}