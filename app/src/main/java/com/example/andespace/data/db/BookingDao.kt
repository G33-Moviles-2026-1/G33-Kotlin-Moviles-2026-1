package com.example.andespace.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY date DESC, startTime DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY date DESC, startTime DESC")
    suspend fun getAllBookings(): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteById(bookingId: String)

    @Query("DELETE FROM bookings")
    suspend fun clearAll()
}
