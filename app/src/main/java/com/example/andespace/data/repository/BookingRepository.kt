package com.example.andespace.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.andespace.model.db.booking.BookingDao
import com.example.andespace.model.db.booking.BookingEntity
import com.example.andespace.model.db.booking.SyncStatus
import com.example.andespace.model.db.booking.toDto
import com.example.andespace.model.db.booking.toEntity
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.httpErrorMessage
import com.example.andespace.data.sync.BookingSyncWorker
import com.example.andespace.model.db.sync.PendingSyncAction
import com.example.andespace.model.db.sync.SyncActionDao
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.google.gson.Gson
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class BookingRepository(
    private val apiService: ApiService,
    private val bookingDao: BookingDao,
    private val syncDao: SyncActionDao,
    private val gson: Gson,
    private val context: Context
) {
    companion object {
        private const val TAG = "BookingsRepository"
        const val ACTION_CREATE_BOOKING = "CREATE_BOOKING"
        const val ACTION_DELETE_BOOKING = "DELETE_BOOKING"
    }

    val bookings: Flow<List<BookingDto>> = bookingDao.getAllBookingsFlow().map { entities ->
        entities
            .filter { it.syncStatus != SyncStatus.PENDING_DELETE }
            .map { it.toDto() }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<BookingSyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    suspend fun refreshBookings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                val remoteBookings = response.body()?.items.orEmpty()

                // Get local bookings
                val localBookings = bookingDao.getAllBookings()

                // Strategy to avoid duplicates:
                // 1. Identify pending creates that match a remote booking by content (Room, Date, Time)
                // 2. Remove those local pending creates because the remote one is the "truth"
                val pendingCreates = localBookings.filter { it.syncStatus == SyncStatus.PENDING_CREATE }.toMutableList()

                val duplicatesToRemove = mutableListOf<BookingEntity>()
                for (pending in pendingCreates) {
                    val match = remoteBookings.find { remote ->
                        remote.roomId == pending.roomId &&
                        remote.date == pending.date &&
                        remote.startTime.take(5) == pending.startTime.take(5) &&
                        remote.endTime.take(5) == pending.endTime.take(5)
                    }
                    if (match != null) {
                        duplicatesToRemove.add(pending)
                    }
                }

                pendingCreates.removeAll(duplicatesToRemove)

                bookingDao.clearAll()
                bookingDao.insertAll(remoteBookings.map { it.toEntity() })
                bookingDao.insertAll(pendingCreates)

                Result.success(Unit)
            } else {
                val code = response.code()
                Result.failure(ApiException(if (code == 401) "SESSION_EXPIRED" else httpErrorMessage(code)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshBookings exception=${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getMyBookings(): Result<List<BookingDto>> = withContext(Dispatchers.IO) {
        // Cache-then-network: This method can be used for explicit refresh
        // but the main data source is the 'bookings' Flow.
        refreshBookings().onSuccess {
             return@withContext Result.success(bookingDao.getAllBookings().map { it.toDto() })
        }.onFailure {
            val cached = bookingDao.getAllBookings().map { it.toDto() }
            if (cached.isNotEmpty()) return@withContext Result.success(cached)
        }
        Result.failure(Exception("Failed to load bookings"))
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> =
        withContext(Dispatchers.IO) {
            // Optimistic insert
            val tempId = "pending_${UUID.randomUUID()}"
            val pendingBooking = BookingEntity(
                id = tempId,
                roomId = request.roomId,
                date = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                purpose = request.purpose,
                status = "PENDING",
                createdAt = null,
                syncStatus = SyncStatus.PENDING_CREATE
            )
            bookingDao.insertAll(listOf(pendingBooking))

            try {
                if (!NetworkMonitor.isOnline.value) {
                    enqueueBookingAction(ACTION_CREATE_BOOKING, gson.toJson(request))
                    scheduleSync()
                    return@withContext Result.failure(Exception("OFFLINE_SYNC_PENDING"))
                }

                val response = apiService.createBooking(request)
                if (response.isSuccessful) {
                    val booking = response.body()
                    if (booking != null) {
                        bookingDao.deleteById(tempId) // Remove temp
                        bookingDao.insertAll(listOf(booking.toEntity()))
                        Result.success(booking)
                    } else {
                        Result.failure(Exception("Error confirming booking"))
                    }
                } else {
                    enqueueBookingAction(ACTION_CREATE_BOOKING, gson.toJson(request))
                    scheduleSync()
                    val errorString = response.errorBody()?.string()
                    val realErrorMessage = try {
                        val jsonObject = JSONObject(errorString ?: "")
                        jsonObject.getString("detail")
                    } catch (e: Exception) {
                        httpErrorMessage(response.code())
                    }
                    Result.failure(Exception(realErrorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBooking exception=${e.message}", e)
                enqueueBookingAction(ACTION_CREATE_BOOKING, gson.toJson(request))
                scheduleSync()
                Result.failure(Exception("OFFLINE_SYNC_PENDING"))
            }
        }

    suspend fun deleteBooking(bookingId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                // Optimistic local update
                val booking = bookingDao.getAllBookings().find { it.id == bookingId }
                if (booking != null) {
                    if (booking.syncStatus == SyncStatus.PENDING_CREATE) {
                        // If it's a pending create, just remove it from local and remove its action
                        bookingDao.deleteById(bookingId)
                        // Need to find and delete the action from syncDao... complex.
                        // For now, let's just mark it as PENDING_DELETE.
                    }
                    bookingDao.insertAll(listOf(booking.copy(syncStatus = SyncStatus.PENDING_DELETE)))
                }

                if (!NetworkMonitor.isOnline.value) {
                    enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
                    scheduleSync()
                    return@withContext Result.success(true)
                }

                val response = apiService.deleteBooking(bookingId)
                if (response.isSuccessful || response.code() == 204) {
                    bookingDao.deleteById(bookingId)
                    Result.success(true)
                } else {
                    enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
                    scheduleSync()
                    Result.success(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteBooking exception=${e.message}", e)
                enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
                scheduleSync()
                Result.success(true)
            }
        }

    private suspend fun enqueueBookingAction(actionType: String, payload: String) {
        syncDao.insertAction(
            PendingSyncAction(
                actionType = actionType,
                payload = payload
            )
        )
    }
}
