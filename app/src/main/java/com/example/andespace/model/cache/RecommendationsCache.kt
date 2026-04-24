package com.example.andespace.model.cache

import android.app.ActivityManager
import android.content.Context
import android.util.ArrayMap
import android.util.LruCache
import com.example.andespace.model.dto.DayRoomRecommendationsOut
import com.example.andespace.model.dto.RecommendedRoomOut
import java.lang.ref.WeakReference

class RecommendationsCache(context: Context) {

    private val dailyCache: LruCache<String, DayRoomRecommendationsOut>
    private val roomPool = ArrayMap<String, WeakReference<RecommendedRoomOut>>()

    init {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isLowRam = activityManager.isLowRamDevice
        val memoryClass = activityManager.memoryClass

        val maxDaysToCache = when {
            isLowRam || memoryClass < 64 -> 3
            memoryClass < 128 -> 7
            else -> 14
        }

        dailyCache = LruCache(maxDaysToCache)
    }

    fun get(date: String): DayRoomRecommendationsOut? {
        return dailyCache.get(date)
    }

    fun put(date: String, data: DayRoomRecommendationsOut) {
        val optimizedData = optimizeMemoryWithWeakReferences(data)
        dailyCache.put(date, optimizedData)
    }

    fun clearCacheOnScheduleChange() {
        dailyCache.evictAll()
        roomPool.clear()
    }

    private fun optimizeMemoryWithWeakReferences(data: DayRoomRecommendationsOut): DayRoomRecommendationsOut {
        val optimizedSlots = data.slots.map { slot ->
            val optimizedRooms = slot.recommended_rooms.map { room ->
                val poolKey = "${room.room_id}_${slot.slot_start}_${slot.slot_end}"
                val pooledRoom = roomPool[poolKey]?.get()
                if (pooledRoom != null && pooledRoom == room) {
                    pooledRoom
                } else {
                    roomPool[poolKey] = WeakReference(room)
                    room
                }
            }
            slot.copy(recommended_rooms = optimizedRooms)
        }
        return data.copy(slots = optimizedSlots)
    }
}