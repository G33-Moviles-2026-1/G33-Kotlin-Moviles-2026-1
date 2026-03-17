package com.example.andespace.model

data class Booking(
    val id: Int,
    val roomName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val sinceHour: Int = 8,
    val sinceMinute: Int = 0,
    val untilHour: Int = 18,
    val untilMinute: Int = 0,
    val purpose: String = "Studying",
    val peopleCount: Int = 1
)
