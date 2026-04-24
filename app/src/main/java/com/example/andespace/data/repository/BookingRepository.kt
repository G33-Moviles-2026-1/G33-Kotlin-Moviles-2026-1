package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.model.db.booking.BookingDao

import com.example.andespace.model.db.booking.toDto
import com.example.andespace.model.db.booking.toEntity
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.httpErrorMessage
import com.example.andespace.model.db.sync.PendingSyncAction
import com.example.andespace.model.db.sync.SyncActionDao
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BookingRepository(
    private val apiService: ApiService,
    private val bookingDao: BookingDao,
    private val syncDao: SyncActionDao,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "BookingsRepository"
        const val ACTION_CREATE_BOOKING = "CREATE_BOOKING"
        const val ACTION_DELETE_BOOKING = "DELETE_BOOKING"
    }

    val bookings: Flow<List<BookingDto>> = bookingDao.getAllBookingsFlow().map { entities ->
        entities.map { it.toDto() }
    }

    suspend fun refreshBookings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                val bookings = response.body()?.items.orEmpty()
                bookingDao.clearAll()
                bookingDao.insertAll(bookings.map { it.toEntity() })
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
        try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                val bookings = response.body()?.items.orEmpty()
                bookingDao.clearAll()
                bookingDao.insertAll(bookings.map { it.toEntity() })
                Result.success(bookings)
            } else {
                val code = response.code()
                Result.failure(ApiException(if (code == 401) "SESSION_EXPIRED" else httpErrorMessage(code)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMyBookings exception=${e.message}", e)
            val cached = bookingDao.getAllBookings().map { it.toDto() }
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
        }
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> =
        withContext(Dispatchers.IO) {
            try {
                if (!NetworkMonitor.isOnline.value) {
                    enqueueBookingAction(ACTION_CREATE_BOOKING, gson.toJson(request))
                    return@withContext Result.failure(Exception("OFFLINE_SYNC_PENDING"))
                }

                val response = apiService.createBooking(request)
                if (response.isSuccessful) {
                    val booking = response.body()
                    if (booking != null) {
                        bookingDao.insertAll(listOf(booking.toEntity()))
                        Result.success(booking)
                    } else {
                        Result.failure(Exception("The booking could not be confirmed. Please try again."))
                    }
                } else {
                    Result.failure(ApiException(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBooking exception=${e.message}", e)
                enqueueBookingAction(ACTION_CREATE_BOOKING, gson.toJson(request))
                Result.failure(Exception("OFFLINE_SYNC_PENDING"))
            }
        }

    suspend fun deleteBooking(bookingId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                // Optimistic local delete
                bookingDao.deleteById(bookingId)

                if (!NetworkMonitor.isOnline.value) {
                    enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
                    return@withContext Result.success(true)
                }

                val response = apiService.deleteBooking(bookingId)
                if (response.isSuccessful || response.code() == 204) {
                    Result.success(true)
                } else {
                    enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
                    Result.success(true) // Return success because it's enqueued
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteBooking exception=${e.message}", e)
                enqueueBookingAction(ACTION_DELETE_BOOKING, bookingId)
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
