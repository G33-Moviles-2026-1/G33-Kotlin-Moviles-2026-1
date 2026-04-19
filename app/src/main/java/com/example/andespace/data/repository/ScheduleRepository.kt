package com.example.andespace.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.SessionExpiredException
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.ScheduleValidator
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.DayRoomRecommendationsOut
import com.example.andespace.model.dto.ManualScheduleIn
import com.example.andespace.model.dto.ScheduleClassesOut
import com.example.andespace.model.dto.WeeklyScheduleOut
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class ScheduleRepository(private val apiService: ApiService, private val context: Context) {
    private val scheduleValidator: ScheduleValidator = ScheduleValidator(apiService)
    private val gson = Gson()
    private val SCHEDULE_FILE_NAME = "offline_schedule_cache.json"
    companion object {
        private const val TAG = "ScheduleRepository"
    }
    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return scheduleValidator.checkIfScheduleExists()
    }

    suspend fun getWeeklySchedule(date: String? = null): WeeklyScheduleOut {
        val cacheKey = date ?: "current"

        return try {
            val response = withTimeoutOrNull(3000L) {
                apiService.getWeeklySchedule(date)
            }

            if (response != null && response.isSuccessful) {
                val schedule = response.body() ?: throw Exception("Empty schedule body")
                saveScheduleLocally(cacheKey, schedule)
                schedule
            } else {
                getScheduleFromCache(cacheKey)
            }
        } catch (e: Exception) {
            if (e is SessionExpiredException) {
                throw e
            }
            getScheduleFromCache(cacheKey)
        }
    }

    private fun saveScheduleLocally(date: String?, schedule: WeeklyScheduleOut) {
        try {
            val cache = getLocalScheduleCache()
            val cacheKey = date ?: "current"
            cache[cacheKey] = schedule
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val currentMonday =
                LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val validTriadKeys = setOf(
                "current",
                currentMonday.format(formatter),
                currentMonday.minusDays(7).format(formatter),
                currentMonday.plusDays(7).format(formatter)
            )
            cache.keys.retainAll(validTriadKeys)
            val file = File(context.filesDir, SCHEDULE_FILE_NAME)
            val jsonString = gson.toJson(cache)
            file.writeText(jsonString)
            Log.d(TAG, "Saved schedule. Strict Triad cache enforced. Total weeks: ${cache.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save schedule locally", e)
        }
    }

    private fun getScheduleFromCache(cacheKey: String): WeeklyScheduleOut {
        val cache = getLocalScheduleCache()
        return cache[cacheKey]
            ?: throw Exception("Not found in cache. Please check your internet connection.")
    }

    private fun getLocalScheduleCache(): MutableMap<String, WeeklyScheduleOut> {
        return try {
            val file = File(context.filesDir, SCHEDULE_FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                // Tell Gson exactly how to reconstruct the Map
                val type = object : TypeToken<MutableMap<String, WeeklyScheduleOut>>() {}.type
                gson.fromJson(jsonString, type) ?: mutableMapOf()
            } else {
                mutableMapOf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read local schedule cache", e)
            mutableMapOf()
        }
    }

    suspend fun uploadIcs(context: Context, fileUri: Uri): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val fileBytes = contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.readBytes()
                }
                    ?: return@withContext Result.failure(Exception("Could not open the selected file"))

                val requestBody = fileBytes.toRequestBody("text/calendar".toMediaTypeOrNull())

                val multipartPart =
                    MultipartBody.Part.createFormData("file", "schedule.ics", requestBody)

                val response = apiService.uploadIcsFile(multipartPart)

                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val backendMessage =
                        extractErrorMessage(response.errorBody()?.string(), response.code())
                    Result.failure(ApiException(backendMessage))
                }
            } catch (_: Exception) {
                Result.failure(ApiException("No internet connection. Could not upload the file."))
            }
        }

    suspend fun uploadManualSchedule(payload: ManualScheduleIn) {
        val response = apiService.uploadManualSchedule(payload)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to upload schedule")
        }
    }
    suspend fun getScheduleClasses(): ScheduleClassesOut {
        val response = apiService.getScheduleClasses()
        if (response.isSuccessful) {
            return response.body() ?: ScheduleClassesOut(emptyList())
        }
        return ScheduleClassesOut(emptyList())
    }

    suspend fun deleteSchedule() {
        val response = apiService.deleteSchedule()
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to delete schedule")
        }
    }

    suspend fun deleteClass(classId: String) {
        val response = apiService.deleteClass(classId)
        if (!response.isSuccessful) {
            val errorDetails = response.errorBody()?.string()
            println("FASTAPI DELETE CLASS ERROR: $errorDetails")
            throw Exception("Error ${response.code()}: Failed to delete class")
        }
    }

    suspend fun getRoomRecommendationsForDay(date: String): DayRoomRecommendationsOut {
        val response = apiService.getRoomRecommendationsForDay(date)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to fetch recommendations")
        }
        return response.body() ?: throw Exception("Empty recommendations body")
    }

    fun hasAnyCachedSchedule(): Boolean {
        return getLocalScheduleCache().isNotEmpty()
    }


}