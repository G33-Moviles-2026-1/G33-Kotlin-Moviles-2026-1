package com.example.andespace.data.repository

import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.db.notification.NotificationDao
import com.example.andespace.model.db.notification.NotificationEntity
import com.example.andespace.model.db.notification.toEntity
import com.example.andespace.model.dto.NotificationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NotificationsRepository(
    private val apiService: ApiService,
    private val notificationDao: NotificationDao
) {
    val notifications: Flow<List<NotificationDto>> = notificationDao.getAllFlow().map { entities ->
        entities.map { it.toDto() }
    }

    val unreadCount: Flow<Int> = notificationDao.unreadCountFlow()

    suspend fun refreshNotifications(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                val body = response.body()!!
                notificationDao.clearAll()
                notificationDao.insertAll(body.items.map { it.toEntity() })
                Result.success(body.unread)
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun markAsRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markNotificationRead(id)
            if (response.isSuccessful) {
                notificationDao.markAsRead(id)
                Result.success(Unit)
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) {
                notificationDao.markAllAsRead()
                Result.success(Unit)
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun hasCachedNotifications(): Boolean = withContext(Dispatchers.IO) {
        notificationDao.count() > 0
    }
}

private fun NotificationEntity.toDto() = com.example.andespace.model.dto.NotificationDto(
    id = id,
    type = type,
    payload = com.example.andespace.model.dto.BookingNotificationPayloadDto(
        friend_username = friendUsername,
        room_id = roomId,
        date = date,
        start_time = startTime,
        end_time = endTime
    ),
    is_read = isRead,
    created_at = createdAt
)
