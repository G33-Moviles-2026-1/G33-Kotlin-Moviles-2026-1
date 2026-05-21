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
import com.example.andespace.data.repository.shared.RepositoryMessages
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

    val bookings: Flow<List<BookingDto>> = bookingDao.getVisibleBookingsFlow().map { entities ->
        entities.map { it.toDto() }
    }

    private data class BookingContentKey(
        val roomId: String,
        val date: String,
        val start5: String,
        val end5: String
    )

    private fun BookingDto.contentKey() = BookingContentKey(
        roomId = roomId,
        date = date,
        start5 = startTime.take(5),
        end5 = endTime.take(5)
    )

    private fun BookingEntity.contentKey() = BookingContentKey(
        roomId = roomId,
        date = date,
        start5 = startTime.take(5),
        end5 = endTime.take(5)
    )

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
                val localBookings = bookingDao.getAllBookings()

                val remoteContentKeys = remoteBookings.map { it.contentKey() }.toHashSet()
                val pendingCreates = localBookings
                    .filter { it.syncStatus == SyncStatus.PENDING_CREATE }
                    .toMutableList()

                val duplicatesToRemove = pendingCreates.filter { pending ->
                    pending.contentKey() in remoteContentKeys
                }
                pendingCreates.removeAll(duplicatesToRemove.toSet())

                duplicatesToRemove.forEach { bookingDao.deleteById(it.id) }

                val remoteEntities = remoteBookings.map { it.toEntity() }
                val remoteIds = remoteEntities.map { it.id }.toHashSet()
                bookingDao.insertAll(remoteEntities)

                if (pendingCreates.isNotEmpty()) {
                    bookingDao.insertAll(pendingCreates)
                }

                val staleSyncedIds = localBookings
                    .filter { it.syncStatus == SyncStatus.SYNCED && it.id !in remoteIds }
                    .map { it.id }
                if (staleSyncedIds.isNotEmpty()) {
                    bookingDao.deleteByIds(staleSyncedIds)
                }

                Result.success(Unit)
            } else {
                val code = response.code()
                Result.failure(ApiException(if (code == 401) "SESSION_EXPIRED" else httpErrorMessage(code)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshBookings exception=${e.message}", e)
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun getMyBookings(): Result<List<BookingDto>> = withContext(Dispatchers.IO) {
        // Cache-then-network: This method can be used for explicit refresh
        // but the main data source is the 'bookings' Flow.
        refreshBookings().onSuccess {
             return@withContext Result.success(bookingDao.getVisibleBookings().map { it.toDto() })
        }.onFailure {
            val cached = bookingDao.getVisibleBookings().map { it.toDto() }
            if (cached.isNotEmpty()) return@withContext Result.success(cached)
        }
        Result.failure(Exception(RepositoryMessages.BOOKING_LOAD_FAILED))
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
                        Result.failure(Exception(RepositoryMessages.BOOKING_CONFIRM_FAILED))
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
                    return@withContext Result.failure(Exception("OFFLINE_SYNC_PENDING"))
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
                Result.failure(Exception("OFFLINE_SYNC_PENDING"))
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
