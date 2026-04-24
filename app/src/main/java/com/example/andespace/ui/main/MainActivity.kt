package com.example.andespace.ui.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.andespace.BuildConfig
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.model.AppDestinations
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.AppViewModelProvider
import com.example.andespace.ui.auth.LoginScreen
import com.example.andespace.ui.auth.RegisterScreen
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.bookings.MainBookingsScreen
import com.example.andespace.ui.components.AndeSpaceBottomBar
import com.example.andespace.ui.components.AndeSpaceTopBar
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.favorites.MainFavoritesScreen
import com.example.andespace.ui.homepage.HomepageViewModel
import com.example.andespace.ui.homepage.HomePageScreen
import com.example.andespace.ui.results.ResultsViewModel
import com.example.andespace.ui.schedule.MainScheduleScreen
import com.example.andespace.ui.schedule.ScheduleViewModel
import com.example.andespace.ui.theme.AndeSpaceTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkMonitor.register(applicationContext, BuildConfig.API_BASE_URL)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val mainUiState by mainViewModel.uiState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkMode = when (mainUiState.themeMode) {
                ThemeMode.AUTOMATIC -> mainUiState.sensorDarkMode
                ThemeMode.SYSTEM    -> isSystemDark
                ThemeMode.LIGHT     -> false
                ThemeMode.DARK      -> true
            }
            AndeSpaceTheme(darkTheme = isDarkMode) {
                AndeSpaceApp(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun AndeSpaceApp(
    viewModel: MainViewModel,
    homepageViewModel: HomepageViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scheduleViewModel: ScheduleViewModel = viewModel(factory = AppViewModelProvider.Factory),
    favoritesViewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val resultsViewModel: ResultsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val detailRoomViewModel: DetailRoomViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val bookingsViewModel: BookingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val recommendationsViewModel: com.example.andespace.ui.recommendations.RecommendationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val isOnline by NetworkMonitor.isOnline.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && uiState.isLoggedIn) {
                favoritesViewModel.onAppForegrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values[0]
                val sensorDark = viewModel.uiState.value.sensorDarkMode
                when {
                    lux < 25f && !sensorDark -> viewModel.setSensorDarkMode(true)
                    lux > 35f && sensorDark  -> viewModel.setSensorDarkMode(false)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    if (uiState.showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginRequiredDialog(navigateToLogin = false) },
            title = {
                Text(
                    text = "Login Required",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(text = "You're trying to access a feature that requires an account. Please log in to continue.",
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissLoginRequiredDialog(navigateToLogin = true)}
                ) {
                    Text("Log in",
                        style = MaterialTheme.typography.bodyMedium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissLoginRequiredDialog(navigateToLogin = false) }
                ) {
                    Text("Return to homepage",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    if (uiState.showSessionExpiredDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSessionExpiredDialog() },
            title = {
                Text(
                    text = "Session Expired",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text("For security reasons, please log in again.",
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSessionExpiredDialog() }
                ) {
                    Text("Got it",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AndeSpaceTopBar(
                isLoggedIn = uiState.isLoggedIn,
                isMenuExpanded = uiState.isUserMenuExpanded,
                themeMode = uiState.themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                onAccountClick = { viewModel.expandUserMenu() },
                onDismissMenu = { viewModel.closeUserMenu() },
                onLoginClick = {
                    viewModel.onDestinationChanged(AppDestinations.LOGIN)
                },
                onRegisterClick = {
                    viewModel.onDestinationChanged(AppDestinations.REGISTER)
                },
                onLogOut = {
                    viewModel.onLogOut()
                    scheduleViewModel.clearScheduleData()
                    favoritesViewModel.clearFavorites()
                }
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = !isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You're offline",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            when (uiState.currentDestination) {

                AppDestinations.CLASSROOMS -> {
                    HomePageScreen(
                        homepageViewModel = homepageViewModel,
                        resultsViewModel = resultsViewModel,
                        detailRoomViewModel = detailRoomViewModel,
                        bookingsViewModel = bookingsViewModel,
                        favoritesViewModel = favoritesViewModel,
                        recommendationsViewModel = recommendationsViewModel,
                        isUserLoggedIn = uiState.isLoggedIn,
                        onRequireLogin = { viewModel.requestLoginRequiredDialog() },
                        onBookingCreatedNavigate = {
                            viewModel.onDestinationChanged(AppDestinations.BOOKINGS)
                        }
                    )
                }

                AppDestinations.LOGIN -> LoginScreen(
                    onLoginSuccess = {
                        viewModel.onLogin()
                        bookingsViewModel.resetRequiresLogin()
                        scheduleViewModel.checkScheduleStatus()
                        favoritesViewModel.refreshFromBackend(force = true)
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    }
                )

                AppDestinations.REGISTER -> RegisterScreen(
                    onRegisterSuccess = {
                        viewModel.onLogin()
                        bookingsViewModel.resetRequiresLogin()
                        scheduleViewModel.clearScheduleData()
                        favoritesViewModel.refreshFromBackend(force = true)
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    }
                )

                AppDestinations.FAVORITES -> {
                    MainFavoritesScreen(
                        favoritesViewModel = favoritesViewModel,
                        onRoomClick = { room ->
                            detailRoomViewModel.setRoom(room = room)
                            homepageViewModel.onShowRoomDetailScreen()
                            viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                        }
                    )
                }

                AppDestinations.BOOKINGS -> {
                    MainBookingsScreen(
                        bookingsViewModel = bookingsViewModel,
                        onRequireLogin = { viewModel.requestLoginRequiredDialog() }
                    )
                }

                AppDestinations.SCHEDULE -> {
                    MainScheduleScreen(
                        scheduleViewModel = scheduleViewModel,
                        onNavigateToRoomDetail = { recommendedRoom ->
                            val mappedRoom = RoomDto(
                                id = recommendedRoom.room_id,
                                name = recommendedRoom.room_id,
                                building = recommendedRoom.building_name,
                                capacity = recommendedRoom.capacity,
                                utilities = emptyList(),
                                waitSeconds = null,
                                matchingWindows = emptyList()
                            )
                            detailRoomViewModel.setRoom(mappedRoom)
                            homepageViewModel.onShowRoomDetailScreen()
                            viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                        }
                    )
                }
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
            text = name,
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
