package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.httpErrorMessage
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.CreateBookingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.andespace.data.repository.shared.ApiException


class BookingRepository(private val apiService: ApiService) {
    companion object {
        private const val TAG = "BookingsRepository"
    }
    suspend fun getMyBookings(): Result<List<BookingDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                Result.success(response.body()?.items.orEmpty())
            } else {
                val code = response.code()
                Result.failure(
                    ApiException(
                        if (code == 401) "SESSION_EXPIRED" else httpErrorMessage(
                            code
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMyBookings exception=${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("The booking could not be confirmed. Please try again."))
                } else {
                    Result.failure(ApiException(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBooking exception=${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
        }

    suspend fun deleteBooking(bookingId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteBooking(bookingId)
                if (response.isSuccessful || response.code() == 204) {
                    Result.success(true)
                } else {
                    Result.failure(ApiException(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteBooking exception=${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))

            }
        }
    
}