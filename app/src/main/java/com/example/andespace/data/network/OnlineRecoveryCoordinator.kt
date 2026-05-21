package com.example.andespace.data.network

import android.util.Log
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.FriendsRepository
import com.example.andespace.data.repository.NotificationsRepository
import com.example.andespace.data.repository.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coalesces reconnect work: one debounced online recovery instead of many parallel ViewModel refreshes.
 */
object OnlineRecoveryCoordinator {
    private const val TAG = "OnlineRecovery"
    private const val DEBOUNCE_MS = 400L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val recoveryMutex = Mutex()
    private var started = false

    private val _recoveryCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val recoveryCompleted: SharedFlow<Unit> = _recoveryCompleted.asSharedFlow()

    fun start(
        syncManager: SyncManager,
        friendsRepository: FriendsRepository,
        notificationsRepository: NotificationsRepository,
        favoritesRepository: FavoritesRepository
    ) {
        if (started) return
        started = true

        scope.launch {
            NetworkMonitor.isOnline
                .filter { it }
                .debounce(DEBOUNCE_MS)
                .collect {
                    if (!NetworkMonitor.isOnline.value) return@collect
                    runRecovery(
                        syncManager = syncManager,
                        friendsRepository = friendsRepository,
                        notificationsRepository = notificationsRepository,
                        favoritesRepository = favoritesRepository
                    )
                }
        }
    }

    private suspend fun runRecovery(
        syncManager: SyncManager,
        friendsRepository: FriendsRepository,
        notificationsRepository: NotificationsRepository,
        favoritesRepository: FavoritesRepository
    ) {
        recoveryMutex.withLock {
            Log.d(TAG, "Starting coalesced online recovery")
            try {
                syncManager.performOnlineRecovery()
                friendsRepository.syncPendingFriendActions()
                favoritesRepository.syncPendingFavoriteActions()
                notificationsRepository.refreshNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Online recovery failed", e)
            }
        }
        _recoveryCompleted.emit(Unit)
        Log.d(TAG, "Online recovery completed")
    }
}
