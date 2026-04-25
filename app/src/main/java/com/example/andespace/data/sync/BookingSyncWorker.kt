package com.example.andespace.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.andespace.AndeSpaceApplication
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.model.db.booking.SyncStatus
import com.example.andespace.model.dto.CreateBookingRequest
import java.io.IOException

class BookingSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as AndeSpaceApplication
        val container = application.container
        val apiService = container.apiService
        val syncDao = container.syncDao
        val bookingRepository = container.bookingRepository
        val gson = container.gson

        val pendingActions = syncDao.getAllPendingActions()
        if (pendingActions.isEmpty()) return Result.success()

        var hasFailure = false

        for (action in pendingActions) {
            try {
                when (action.actionType) {
                    BookingRepository.ACTION_CREATE_BOOKING -> {
                        val request = gson.fromJson(action.payload, CreateBookingRequest::class.java)
                        val response = apiService.createBooking(request)
                        if (response.isSuccessful) {
                            syncDao.deleteAction(action.id)
                        } else {
                            if (response.code() in (400..499)) {
                                // It's a client error (e.g. room already occupied), so it failed
                                // We should remove the local pending booking that represents this failure
                                val localBookings = application.container.bookingDao.getAllBookings()
                                val match = localBookings.find { b ->
                                    b.syncStatus == SyncStatus.PENDING_CREATE &&
                                    b.roomId == request.roomId &&
                                    b.date == request.date &&
                                    b.startTime.take(5) == request.startTime.take(5)
                                }
                                if (match != null) {
                                    application.container.bookingDao.deleteById(match.id)
                                }
                                syncDao.deleteAction(action.id)
                            } else {
                                hasFailure = true
                            }
                        }
                    }
                    BookingRepository.ACTION_DELETE_BOOKING -> {
                        val bookingId = action.payload
                        val response = apiService.deleteBooking(bookingId)
                        if (response.isSuccessful || response.code() == 204 || response.code() == 404) {
                            syncDao.deleteAction(action.id)
                        } else {
                            hasFailure = true
                        }
                    }
                }
            } catch (e: IOException) {
                return Result.retry()
            } catch (e: Exception) {
                Log.e("BookingSyncWorker", "Error processing sync action ${action.id}", e)
                syncDao.deleteAction(action.id)
            }
        }

        bookingRepository.refreshBookings()

        return if (hasFailure) Result.retry() else Result.success()
    }
}
