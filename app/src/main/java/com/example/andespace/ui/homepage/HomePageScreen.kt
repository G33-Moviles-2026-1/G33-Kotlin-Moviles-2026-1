package com.example.andespace.ui.homepage

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.bookings.MainMakeBookingScreen
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.detailRoom.RoomDetailScreen
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.recommendations.RecommendationsScreen
import com.example.andespace.ui.recommendations.RecommendationsViewModel
import com.example.andespace.ui.results.ResultsScreen
import com.example.andespace.ui.results.ResultsViewModel

@Composable
fun HomePageScreen(
    homepageViewModel: HomepageViewModel,
    resultsViewModel: ResultsViewModel,
    detailRoomViewModel: DetailRoomViewModel,
    bookingsViewModel: BookingsViewModel,
    recommendationsViewModel: RecommendationsViewModel,
    favoritesViewModel: FavoritesViewModel,
    isUserLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
    onBookingCreatedNavigate: () -> Unit
) {
    val homepageState by homepageViewModel.uiState.collectAsState()

    BackHandler(enabled = homepageState.contentScreen != ContentScreen.HOME) {
        homepageViewModel.onBackPressedInSearchFlow()
    }

    when (homepageState.contentScreen) {
        ContentScreen.HOME -> HomeSearchScreen(
            homepageViewModel = homepageViewModel,
            resultsViewModel = resultsViewModel
        )

        ContentScreen.RESULTS -> ResultsScreen(
            resultsViewModel = resultsViewModel,
            favoritesViewModel = favoritesViewModel,
            detailRoomViewModel = detailRoomViewModel,
            homepageViewModel = homepageViewModel,
            isUserLoggedIn = isUserLoggedIn,
            onRequireLogin = onRequireLogin
        )

        ContentScreen.ROOM_DETAIL -> {
            RoomDetailScreen(
                detailRoomViewModel = detailRoomViewModel,
                favoritesViewModel = favoritesViewModel,
                homepageViewModel = homepageViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onRequireLogin = onRequireLogin,
                onNavigateToNavigation = {
                    homepageViewModel.onNavigateToNavigation(it)
                }
            )
        }

        ContentScreen.MAKE_BOOKING -> {
            MainMakeBookingScreen(
                detailRoomViewModel = detailRoomViewModel,
                bookingsViewModel = bookingsViewModel,
                onBookingCreatedNavigate = onBookingCreatedNavigate
            )
        }

        ContentScreen.AUTO_SEARCH -> {
            RecommendationsScreen(viewModel = recommendationsViewModel)
        }

    }
}
