package com.example.andespace.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.andespace.data.db.PendingSyncAction
import com.example.andespace.data.db.SyncActionDao
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.ScheduleValidator
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.DayRoomRecommendationsOut
import com.example.andespace.model.dto.ManualClassIn
import com.example.andespace.model.dto.ManualScheduleIn
import com.example.andespace.model.dto.ScheduleClassOccurrenceOut
import com.example.andespace.model.dto.WeeklyScheduleOut
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID


class ScheduleRepository(
    private val syncDao: SyncActionDao,
    private val apiService: ApiService,
    private val context: Context
) {
    private val scheduleValidator: ScheduleValidator = ScheduleValidator(apiService)
    private val gson = Gson()
    private val SCHEDULE_FILE_NAME = "offline_schedule_cache.json"
    private val fileMutex = Mutex()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val MINIMUM_FREE_SPACE_BYTES = 5 * 1024 * 1024L
    companion object {
        private const val TAG = "ScheduleRepository"
    }

    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return if (hasAnyCachedSchedule()) {
            Result.success(true)
        } else {
            scheduleValidator.checkIfScheduleExists()
        }
    }

    suspend fun getWeeklySchedule(date: String? = null): WeeklyScheduleOut {
        val cacheKey = date ?: "current"
        val cache = getLocalScheduleCache()

        if (cache.isEmpty()) {
            throw ScheduleNotFoundException()
        }
        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]!!
        }
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val fallbackDate = try {
            if (cacheKey == "current") {
                LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            } else {
                LocalDate.parse(cacheKey, formatter).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }
        } catch (e: Exception) {
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        return WeeklyScheduleOut(
            week_start = fallbackDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            week_end = fallbackDate.plusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE),
            occurrences = emptyList()
        )
    }

    fun triggerSilentBackgroundSync() {
        repoScope.launch {
            try {
                syncEntireScheduleFromBackend()
                Log.d(TAG, "Silent background sync completed successfully.")
            } catch (e: Exception) {
                Log.d(TAG, "Silent background sync aborted (Offline or Unauthenticated).")
            }
        }
    }

    suspend fun clearLocalCacheOnLogout() {
        fileMutex.withLock {
            try {
                val file = File(context.filesDir, SCHEDULE_FILE_NAME)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Local schedule cache wiped successfully on logout.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to wipe local schedule cache on logout", e)
            }
        }
    }

    suspend fun syncEntireScheduleFromBackend() {
        try {
            val response = apiService.getScheduleClasses()
            if (response.isSuccessful) {
                val classes = response.body()?.classes ?: emptyList()

                fileMutex.withLock {
                    val existingCache = getLocalScheduleCacheUnsafe()

                    val pendingActions = syncDao.getAllPendingActions()

                    if (pendingActions.any { it.actionType == "DELETE_SCHEDULE" }) return@withLock

                    val validOfflineIds = pendingActions.mapNotNull { it.localClassId }.toSet()
                    val pendingDeletions = pendingActions
                        .filter { it.actionType == "DELETE_CLASS" }
                        .map { it.payload }
                        .toSet()

                    val offlineClassesToPreserve = existingCache.values
                        .flatMap { it.occurrences }
                        .filter { it.class_id in validOfflineIds }

                    val newCache = mutableMapOf<String, WeeklyScheduleOut>()
                    if (classes.isEmpty()) return@withLock
                    val minDateStr = classes.minOfOrNull { it.start_date } ?: return@withLock
                    val maxDateStr = classes.maxOfOrNull { it.end_date } ?: return@withLock

                    val classStartDate = LocalDate.parse(minDateStr)
                    val classEndDate = LocalDate.parse(maxDateStr)

                    var currentMonday = classStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val endMonday = classEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

                    while (!currentMonday.isAfter(endMonday)) {
                        val cacheKey = currentMonday.format(formatter)
                        newCache[cacheKey] = WeeklyScheduleOut(
                            week_start = currentMonday.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            week_end = currentMonday.plusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE),
                            occurrences = emptyList()
                        )
                        currentMonday = currentMonday.plusWeeks(1)
                    }

                    val dayMapping = mapOf(
                        "mo" to DayOfWeek.MONDAY, "monday" to DayOfWeek.MONDAY,
                        "tu" to DayOfWeek.TUESDAY, "tuesday" to DayOfWeek.TUESDAY,
                        "we" to DayOfWeek.WEDNESDAY, "wednesday" to DayOfWeek.WEDNESDAY,
                        "th" to DayOfWeek.THURSDAY, "thursday" to DayOfWeek.THURSDAY,
                        "fr" to DayOfWeek.FRIDAY, "friday" to DayOfWeek.FRIDAY,
                        "sa" to DayOfWeek.SATURDAY, "saturday" to DayOfWeek.SATURDAY,
                        "su" to DayOfWeek.SUNDAY, "sunday" to DayOfWeek.SUNDAY
                    )

                    classes.forEach { backendClass ->
                        if (backendClass.class_id in pendingDeletions) return@forEach
                        val start = LocalDate.parse(backendClass.start_date)
                        val end = LocalDate.parse(backendClass.end_date)
                        newCache.forEach { (key, weeklySchedule) ->
                            val updatedOccurrences = weeklySchedule.occurrences.toMutableList()
                            val weekStart = LocalDate.parse(weeklySchedule.week_start)

                            backendClass.weekdays.forEach { shortDay ->
                                val targetDayOfWeek = dayMapping[shortDay.lowercase()] ?: return@forEach
                                val occurrenceDate = weekStart.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))

                                if (!occurrenceDate.isBefore(start) && !occurrenceDate.isAfter(end)) {
                                    updatedOccurrences.add(
                                        ScheduleClassOccurrenceOut(
                                            class_id = backendClass.class_id,
                                            title = backendClass.title,
                                            location_text = backendClass.location_text,
                                            room_id = backendClass.room_id,
                                            date = occurrenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                            weekday = targetDayOfWeek.name.lowercase(),
                                            start_time = backendClass.start_time,
                                            end_time = backendClass.end_time
                                        )
                                    )
                                }
                            }
                            newCache[key] = weeklySchedule.copy(occurrences = updatedOccurrences)
                        }
                    }
                    offlineClassesToPreserve.forEach { offlineClass ->
                        newCache.forEach { (key, weeklySchedule) ->
                            val updatedOccurrences = weeklySchedule.occurrences.toMutableList()
                            val weekStart = LocalDate.parse(weeklySchedule.week_start)
                            val targetDayOfWeek = DayOfWeek.valueOf(offlineClass.weekday.uppercase())
                            val occurrenceDate = weekStart.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
                            if (occurrenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE) == offlineClass.date) {
                                updatedOccurrences.add(offlineClass)
                            }
                            newCache[key] = weeklySchedule.copy(occurrences = updatedOccurrences)
                        }
                    }
                    writeCacheToDisk(newCache)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync entire schedule", e)
        }
    }

    private fun writeCacheToDisk(cache: MutableMap<String, WeeklyScheduleOut>) {
        val file = File(context.filesDir, SCHEDULE_FILE_NAME)

        if (context.filesDir.usableSpace < MINIMUM_FREE_SPACE_BYTES) {
            Log.e(TAG, "CRITICAL: Device is critically low on space. Aborting cache write to prevent OS crash.")
            return
        }
        try {
            file.writeText(gson.toJson(cache))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write schedule to disk", e)
        }
    }

    suspend fun syncManualClassWithBackend(newClass: ManualClassIn) {
        val existingResponse = apiService.getScheduleClasses()
        val existingClasses = if (existingResponse.isSuccessful) existingResponse.body()?.classes ?: emptyList() else emptyList()

        val existingClassesToUpload = existingClasses.map { oldClass ->
            ManualClassIn(
                title = oldClass.title ?: "Unknown",
                location_text = oldClass.location_text,
                room_id = oldClass.room_id,
                start_date = oldClass.start_date,
                end_date = oldClass.end_date,
                start_time = oldClass.start_time,
                end_time = oldClass.end_time,
                weekdays = oldClass.weekdays
            )
        }
        val payload = ManualScheduleIn(classes = existingClassesToUpload + newClass)
        apiService.uploadManualSchedule(payload)
    }


    suspend fun uploadManualSchedule(newClass: ManualClassIn) {
        val fakeId = injectClassIntoLocalCache(newClass)
        repoScope.launch {
            try {
                syncManualClassWithBackend(newClass)
            } catch (e: Exception) {
                val payloadString = gson.toJson(newClass)
                syncDao.insertAction(
                    PendingSyncAction(
                        actionType = "ADD_CLASS",
                        payload = payloadString,
                        localClassId = fakeId
                    )
                )
                Log.w(TAG, "Offline: Manual class queued for SyncManager.")
            }
        }
    }

    suspend fun deleteClass(classId: String) {
        removeClassFromLocalCache(classId)
        repoScope.launch {
            try {
                syncDeleteClassWithBackend(classId)
            } catch (e: Exception) {
                syncDao.insertAction(PendingSyncAction(actionType = "DELETE_CLASS", payload = classId))
                Log.w(TAG, "Offline: Class deletion queued for SyncManager.")
            }
        }
    }

    suspend fun deleteSchedule() {
        File(context.filesDir, SCHEDULE_FILE_NAME).delete()
        repoScope.launch {
            try {
                syncDeleteScheduleWithBackend()
            } catch (e: Exception) {
                syncDao.insertAction(PendingSyncAction(actionType = "DELETE_SCHEDULE", payload = ""))
                Log.w(TAG, "Offline: Schedule deletion queued for SyncManager.")
            }
        }
    }

    suspend fun syncDeleteClassWithBackend(classId: String) {
        apiService.deleteClass(classId)
    }


    suspend fun syncDeleteScheduleWithBackend() {
        apiService.deleteSchedule()
    }

    private suspend fun injectClassIntoLocalCache(newClass: ManualClassIn): String {
        return fileMutex.withLock {
            val cache = getLocalScheduleCacheUnsafe()
            val fakeId = "offline_${UUID.randomUUID()}"

            val dayMapping = mapOf(
                "monday" to DayOfWeek.MONDAY,
                "tuesday" to DayOfWeek.TUESDAY,
                "wednesday" to DayOfWeek.WEDNESDAY,
                "thursday" to DayOfWeek.THURSDAY,
                "friday" to DayOfWeek.FRIDAY,
                "saturday" to DayOfWeek.SATURDAY,
                "sunday" to DayOfWeek.SUNDAY
            )

            val classStartDate = try {
                LocalDate.parse(newClass.start_date)
            } catch (e: Exception) {
                return@withLock ""
            }
            val classEndDate = try {
                LocalDate.parse(newClass.end_date)
            } catch (e: Exception) {
                return@withLock ""
            }

            var currentMonday =
                classStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endMonday = classEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

            while (!currentMonday.isAfter(endMonday)) {
                val cacheKey = currentMonday.format(formatter)
                if (!cache.containsKey(cacheKey)) {
                    cache[cacheKey] = WeeklyScheduleOut(
                        week_start = currentMonday.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        week_end = currentMonday.plusDays(6)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE),
                        occurrences = emptyList()
                    )
                }
                currentMonday = currentMonday.plusWeeks(1)
            }

            cache.forEach { (key, weeklySchedule) ->
                val updatedOccurrences = weeklySchedule.occurrences.toMutableList()
                val weekStart = try {
                    LocalDate.parse(weeklySchedule.week_start)
                } catch (e: Exception) {
                    return@forEach
                }

                newClass.weekdays.forEach { uiDay ->
                    val targetDayOfWeek = dayMapping[uiDay] ?: return@forEach
                    val occurrenceDate =
                        weekStart.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))

                    if (!occurrenceDate.isBefore(classStartDate) && !occurrenceDate.isAfter(
                            classEndDate
                        )
                    ) {
                        updatedOccurrences.add(
                            ScheduleClassOccurrenceOut(
                                class_id = fakeId,
                                title = newClass.title,
                                location_text = newClass.location_text,
                                room_id = newClass.room_id,
                                date = occurrenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                weekday = uiDay,
                                start_time = newClass.start_time,
                                end_time = newClass.end_time
                            )
                        )
                    }
                }
                cache[key] = weeklySchedule.copy(occurrences = updatedOccurrences)
            }
            writeCacheToDisk(cache)
            fakeId
        }
    }

    private suspend fun removeClassFromLocalCache(classId: String) {
        fileMutex.withLock {
            val cache = getLocalScheduleCacheUnsafe()

            cache.forEach { (key, weeklySchedule) ->
                val updatedList = weeklySchedule.occurrences.filter { it.class_id != classId }
                cache[key] = weeklySchedule.copy(occurrences = updatedList)
            }

            writeCacheToDisk(cache)
        }
    }

    private fun getLocalScheduleCacheUnsafe(): MutableMap<String, WeeklyScheduleOut> {
        return try {
            val file = File(context.filesDir, SCHEDULE_FILE_NAME)
            if (file.exists()) {
                val type = object : TypeToken<MutableMap<String, WeeklyScheduleOut>>() {}.type
                gson.fromJson(file.readText(), type) ?: mutableMapOf()
            } else { mutableMapOf() }
        } catch (e: Exception) { mutableMapOf() }
    }


    private suspend fun getLocalScheduleCache(): MutableMap<String, WeeklyScheduleOut> {
        return fileMutex.withLock {
            getLocalScheduleCacheUnsafe()
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
    suspend fun getRoomRecommendationsForDay(date: String): DayRoomRecommendationsOut {
        val response = apiService.getRoomRecommendationsForDay(date)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to fetch recommendations")
        }
        return response.body() ?: throw Exception("Empty recommendations body")
    }

    suspend fun hasAnyCachedSchedule(): Boolean {
        return getLocalScheduleCache().isNotEmpty()
    }
}

class ScheduleNotFoundException : Exception("No schedule exists locally. Trigger onboarding.")