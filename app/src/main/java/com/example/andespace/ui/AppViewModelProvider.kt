package com.example.andespace.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.andespace.AndeSpaceApplication
import com.example.andespace.ui.auth.AuthViewModel
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.homepage.HomepageViewModel
import com.example.andespace.ui.main.MainViewModel
import com.example.andespace.ui.results.ResultsViewModel
import com.example.andespace.ui.schedule.ScheduleViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MainViewModel(
                authRepository = andeSpaceApplication().container.authRepository,
                analyticsRepository = andeSpaceApplication().container.analyticsRepository
            )
        }
        initializer {
            ScheduleViewModel(
                repository = andeSpaceApplication().container.scheduleRepository,
            )
        }
        initializer {
            ResultsViewModel(
                repository = andeSpaceApplication().container.roomRepository,
            )
        }
        initializer {
            AuthViewModel(
                repository = andeSpaceApplication().container.authRepository,
            )
        }
        initializer {
            BookingsViewModel(
                repository = andeSpaceApplication().container.bookingRepository,
            )
        }
        initializer {
            HomepageViewModel(
                repository = andeSpaceApplication().container.roomRepository,
            )
        }

        initializer {
            DetailRoomViewModel(
                repository = andeSpaceApplication().container.roomRepository,
            )
        }
        initializer{
            FavoritesViewModel(
                repository = andeSpaceApplication().container.favoritesRepository,
            )
        }

    }
}

fun CreationExtras.andeSpaceApplication(): AndeSpaceApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AndeSpaceApplication)