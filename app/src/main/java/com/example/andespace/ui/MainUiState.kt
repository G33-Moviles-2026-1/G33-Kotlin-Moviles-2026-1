package com.example.andespace.ui

import com.example.andespace.model.AppDestinations
import com.example.andespace.model.Booking

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val selectedBooking: Booking? = null
)
