package com.example.andespace

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.MainViewModel
import com.example.andespace.ui.ThemeMode
import com.example.andespace.ui.auth.LoginScreen
import com.example.andespace.ui.auth.RegisterScreen
import com.example.andespace.ui.bookings.MainBookingsScreen
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.components.AndeSpaceBottomBar
import com.example.andespace.ui.components.AndeSpaceTopBar
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.homepage.MainClassroomsScreen
import com.example.andespace.ui.homepage.HomepageViewModel
import com.example.andespace.ui.results.ResultsViewModel
import com.example.andespace.ui.schedule.MainScheduleScreen
import com.example.andespace.ui.schedule.ScheduleViewModel
import com.example.andespace.ui.favorites.FavoritesViewModel
import com.example.andespace.ui.favorites.MainFavoritesScreen
import com.example.andespace.ui.screen.HistoryScreen
import com.example.andespace.ui.theme.AndeSpaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
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
    viewModel: MainViewModel = viewModel(),
    homepageViewModel: HomepageViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
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

    val resultsViewModel: ResultsViewModel = viewModel()
    val detailRoomViewModel: DetailRoomViewModel = viewModel()
    val bookingsViewModel: BookingsViewModel = viewModel()
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
            AndeSpaceBottomBar(
                currentDestination = uiState.currentDestination,
                onDestinationChanged = { destination ->
                    viewModel.onDestinationChanged(destination)
                    if (destination == AppDestinations.CLASSROOMS) {
                        homepageViewModel.resetToHome()
                    }
                }
            )
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
                    MainClassroomsScreen(
                        homepageViewModel = homepageViewModel,
                        resultsViewModel = resultsViewModel,
                        detailRoomViewModel = detailRoomViewModel,
                        bookingsViewModel = bookingsViewModel,
                        favoritesViewModel = favoritesViewModel,
                        isUserLoggedIn = uiState.isLoggedIn,
                        onRequireLogin = { viewModel.onDestinationChanged(AppDestinations.LOGIN) },
                        onBookingCreatedNavigate = {
                            viewModel.onDestinationChanged(AppDestinations.BOOKINGS)
                        }
                    )
                }

                AppDestinations.HISTORY -> HistoryScreen()

                AppDestinations.LOGIN -> LoginScreen(
                    onLoginSuccess = {
                        viewModel.onLogin()
                        bookingsViewModel.resetRequiresLogin()
                        scheduleViewModel.checkScheduleStatus()
                        favoritesViewModel.refreshFromBackend()
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    },
                    onNavigateToRegister = {
                        viewModel.onDestinationChanged(AppDestinations.REGISTER)
                    }
                )

                AppDestinations.REGISTER -> RegisterScreen(
                    onRegisterSuccess = {
                        viewModel.onLogin()
                        bookingsViewModel.resetRequiresLogin()
                        scheduleViewModel.clearScheduleData()
                        favoritesViewModel.refreshFromBackend()
                        viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                    },
                    onNavigateToLogin = {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    }
                )

                AppDestinations.FAVORITES -> {
                    if (uiState.isLoggedIn) {
                        MainFavoritesScreen(
                            favoritesViewModel = favoritesViewModel,
                            onRoomClick = { room ->
                                detailRoomViewModel.setRoom(room = room)
                                homepageViewModel.onShowRoomDetailScreen()
                                viewModel.onDestinationChanged(AppDestinations.CLASSROOMS)
                            }
                        )
                    } else {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    }
                }

                AppDestinations.BOOKINGS -> {
                    if (uiState.isLoggedIn) {
                        MainBookingsScreen(
                            bookingsViewModel = bookingsViewModel,
                            onRequireLogin = { viewModel.onDestinationChanged(AppDestinations.LOGIN) }
                        )
                    } else {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    }
                }

                AppDestinations.SCHEDULE -> {
                    if (uiState.isLoggedIn) {
                        MainScheduleScreen(scheduleViewModel = scheduleViewModel)
                    } else {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    }
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
