package com.example.andespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.MainViewModel
import com.example.andespace.ui.auth.LoginScreen
import com.example.andespace.ui.auth.RegisterScreen
import com.example.andespace.ui.bookings.BookingsContentScreen
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.bookings.EditBookingScreen
import com.example.andespace.ui.bookings.MakeBookingScreen
import com.example.andespace.ui.bookings.MyBookingsScreen
import com.example.andespace.ui.components.AndeSpaceBottomBar
import com.example.andespace.ui.components.AndeSpaceTopBar
import com.example.andespace.ui.cookie.CookieScreen
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.detailRoom.RoomDetailScreen
import com.example.andespace.ui.homepage.ContentScreen
import com.example.andespace.ui.homepage.HomepageContent
import com.example.andespace.ui.homepage.HomePageScreen
import com.example.andespace.ui.homepage.HomepageViewModel
import com.example.andespace.ui.results.ResultsViewModel
import com.example.andespace.ui.screen.HistoryScreen
import com.example.andespace.ui.theme.AndeSpaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndeSpaceTheme {
                AndeSpaceApp()
            }
        }
    }
}

@Composable
fun AndeSpaceApp(
    viewModel: MainViewModel = viewModel(),
    homepageViewModel: HomepageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val homepageState by homepageViewModel.uiState.collectAsState()
    val resultsViewModel: ResultsViewModel = viewModel()
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    val detailRoomViewModel: DetailRoomViewModel = viewModel()
    val detailRoomUiState by detailRoomViewModel.uiState.collectAsState()
    val bookingsViewModel: BookingsViewModel = viewModel()
    val bookingsUiState by bookingsViewModel.uiState.collectAsState()

    val isOnAuthScreen = !uiState.isLoggedIn &&
            (uiState.currentDestination == AppDestinations.LOGIN ||
                    uiState.currentDestination == AppDestinations.REGISTER)

    Scaffold(
        topBar = {
            AndeSpaceTopBar(
                isLoggedIn = uiState.isLoggedIn,
                isMenuExpanded = uiState.isUserMenuExpanded,
                onAccountClick = { viewModel.expandUserMenu() },
                onDismissMenu = { viewModel.closeUserMenu() },
                onLoginClick = {
                    viewModel.onDestinationChanged(AppDestinations.LOGIN)
                },
                onRegisterClick = {
                    viewModel.onDestinationChanged(AppDestinations.REGISTER)
                },
                onHistoryClick = { viewModel.onDestinationChanged(AppDestinations.HISTORY) },
                onLogOut = {
                    viewModel.onLogOut()
                }
            )
        },
        bottomBar = {
            if (!isOnAuthScreen) {
                AndeSpaceBottomBar(
                    currentDestination = uiState.currentDestination,
                    onDestinationChanged = { destination ->
                        viewModel.onDestinationChanged(destination)
                        if (destination == AppDestinations.CLASSROOMS) {
                            homepageViewModel.resetToHome()
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState.currentDestination) {
                AppDestinations.CLASSROOMS -> if (homepageState.contentScreen == ContentScreen.ROOM_DETAIL) {
                    RoomDetailScreen(
                        room = detailRoomUiState.room,
                        selectedDate = detailRoomUiState.selectedDate,
                        isLoadingAvailability = detailRoomUiState.isLoadingAvailability,
                        availabilityError = detailRoomUiState.availabilityError,
                        onDateChange = { dateValue -> detailRoomViewModel.onDateChange(dateValue) }
                    )
                } else {
                    HomepageContent(
                        contentScreen = homepageState.contentScreen,
                        isSearching = resultsUiState.isSearching,
                        isUserLoggedIn = uiState.isLoggedIn,
                        searchError = resultsUiState.errorMessage,
                        rooms = resultsUiState.rooms,
                        currentPage = resultsUiState.currentPage,
                        totalPages = resultsUiState.totalPages,
                        onSearchClick = { params ->
                            resultsViewModel.onSearchClick(
                                params = params,
                                isUserLoggedIn = uiState.isLoggedIn
                            )
                            homepageViewModel.onShowResults()
                        },
                        onFiltersOpened = { homepageViewModel.onFiltersOpened() },
                        onRoomClick = { room ->
                            resultsViewModel.onRoomClick(room)
                            detailRoomViewModel.setRoom(
                                room = room,
                                selectedDate = resultsUiState.selectedSearchDate
                            )
                            homepageViewModel.onShowRoomDetailScreen()
                        },
                        onPrevPage = { resultsViewModel.onPreviousPage(isUserLoggedIn = uiState.isLoggedIn) },
                        onNextPage = { resultsViewModel.onNextPage(isUserLoggedIn = uiState.isLoggedIn) }
                    )
                    ContentScreen.MAKE_BOOKING -> {
                        val room = detailRoomUiState.room
                        val roomId = room?.id ?: ""
                        val date = detailRoomUiState.selectedDate ?: ""
                        val windows = room?.matchingWindows.orEmpty()

                        if (bookingsUiState.bookingCreatedSuccess) {
                            LaunchedEffect(Unit) {
                                bookingsViewModel.consumeBookingCreatedSuccess()
                                viewModel.onDestinationChanged(AppDestinations.BOOKINGS)
                                homepageViewModel.resetToHome()
                            }
                        }

                        MakeBookingScreen(
                            roomId = roomId,
                            selectedDate = date,
                            availableWindows = windows,
                            isLoadingSlots = detailRoomUiState.isLoadingAvailability,
                            isCreating = bookingsUiState.isCreating,
                            errorMessage = bookingsUiState.createError,
                            onDateChanged = { newDate ->
                                detailRoomViewModel.onDateChange(newDate)
                            },
                            onBook = { request -> bookingsViewModel.onCreateBooking(request) }
                        )
                    }
                }
                AppDestinations.HISTORY -> HistoryScreen()
                AppDestinations.LOGIN -> LoginScreen(
                    onLoginSuccess = {
                        viewModel.onLogin()
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    },
                    onNavigateToRegister = {
                        viewModel.onDestinationChanged(AppDestinations.REGISTER)
                    }
                )
                AppDestinations.REGISTER -> RegisterScreen(
                    onRegisterSuccess = {
                        viewModel.onLogin()
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    },
                    onNavigateToLogin = {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    }
                )
                AppDestinations.FAVORITES -> CookieScreen()
                AppDestinations.BOOKINGS -> {
                    LaunchedEffect(uiState.currentDestination) {
                        bookingsViewModel.loadBookings()
                    }

                    if (bookingsUiState.requiresLogin) {
                        LaunchedEffect(bookingsUiState.requiresLogin) {
                            viewModel.onDestinationChanged(AppDestinations.LOGIN)
                        }
                    } else when (bookingsUiState.contentScreen) {
                        BookingsContentScreen.LIST -> MyBookingsScreen(
                            bookings = bookingsUiState.bookings,
                            isLoading = bookingsUiState.isLoading,
                            errorMessage = bookingsUiState.errorMessage,
                            onDeleteBooking = { bookingsViewModel.onDeleteBooking(it) },
                            onEditBooking = { bookingsViewModel.onEditBooking(it) }
                        )
                        BookingsContentScreen.EDIT -> {
                            bookingsUiState.selectedBooking?.let { booking ->
                                EditBookingScreen(
                                    booking = booking,
                                    isSaving = bookingsUiState.isSaving,
                                    onSave = { request, oldId ->
                                        bookingsViewModel.onSaveBooking(request, oldId)
                                    },
                                    onCancel = { bookingsViewModel.onCancelEdit() }
                                )
                            }
                        }
                    }
                }
                else -> Greeting(
                    name = if (uiState.isLoading) "Loading..." else uiState.currentDestination.label
                )
            }

            if (uiState.isUserMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.closeUserMenu() }
                )
            }
        }
    }
}

@Composable
fun AssetIcon(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/$assetPath")
            .build(),
        imageLoader = imageLoader
    )

    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Section: $name",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndeSpaceTheme {
        Greeting("Android")
    }
}
