package com.example.andespace.data.repository.sync

import android.util.Log
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.AnalyticsRepository // Assuming you split this out!
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SyncManager(
    private val analyticsRepository: AnalyticsRepository,

) {
    private val TAG = "SyncManager"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isSyncing = false

    fun startMonitoring() {
        Log.d(TAG, "SyncManager started monitoring the network...")

        scope.launch {
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    performSync()
                } else {
                    Log.d(TAG, "SyncManager sleeping (Offline)")
                }
            }
        }
    }

    private suspend fun performSync() {
        if (isSyncing) return

        isSyncing = true
        try {
            Log.d(TAG, "Network is online! Orchestrating background sync...")

            // --- THE UNIFIED SYNC PIPELINE ---

            // 1. Send all pending analytics
            analyticsRepository.flushPendingAnalytics()

            // 2. Upload any schedules created offline (later)
            // scheduleRepository.uploadPendingSchedules()

            // 3. Fetch latest data to update Room database (later)
            // roomRepository.refreshLocalCache()

            Log.d(TAG, "Background sync completed successfully!")

        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed: ${e.message}", e)
        } finally {
            isSyncing = false // Unlock when finished or if it crashes
        }
    }
}