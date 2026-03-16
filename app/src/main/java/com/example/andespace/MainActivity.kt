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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.andespace.ui.ContentScreen
import com.example.andespace.ui.MainViewModel
import com.example.andespace.ui.auth.LoginScreen
import com.example.andespace.ui.auth.RegisterScreen
import com.example.andespace.ui.components.AndeSpaceBottomBar
import com.example.andespace.ui.components.AndeSpaceTopBar
import com.example.andespace.ui.screen.HistoryScreen
import com.example.andespace.ui.screen.HomePageScreen
import com.example.andespace.ui.screen.ResultsScreen
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
fun AndeSpaceApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var displayMenu by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AndeSpaceTopBar(
                isLoggedIn = uiState.isLoggedIn,
                isMenuExpanded = displayMenu,
                onAccountClick = { displayMenu = true },
                onDismissMenu = { displayMenu = false },
                onLoginClick = {
                    viewModel.onDestinationChanged(AppDestinations.LOGIN)
                    displayMenu = false
                },
                onRegisterClick = {
                    viewModel.onDestinationChanged(AppDestinations.REGISTER)
                    displayMenu = false
                },
                onHistoryClick = { viewModel.onHistoryClick() },
                onLogOut = {
                    viewModel.onLogOut()
                    displayMenu = false
                }
            )
        },
        bottomBar = {
            AndeSpaceBottomBar(
                currentDestination = uiState.currentDestination,
                onDestinationChanged = { viewModel.onDestinationChanged(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            when (uiState.currentDestination) {
                AppDestinations.CLASSROOMS -> when (uiState.contentScreen) {
                    ContentScreen.HOME -> HomePageScreen(
                        isSearching = uiState.isSearching,
                        searchError = uiState.searchError,
                        onSearchClick = { params -> viewModel.onSearchClick(params) },
                        onFiltersOpened = { viewModel.onFiltersOpened() }
                    )
                    ContentScreen.RESULTS -> ResultsScreen()
                    ContentScreen.HISTORY -> HistoryScreen()
                }
                AppDestinations.HISTORY -> HistoryScreen()
                AppDestinations.LOGIN -> LoginScreen()
                AppDestinations.REGISTER -> RegisterScreen()
                else -> Greeting(
                    name = if (uiState.isLoading) "Loading..." else uiState.currentDestination.label
                )
            }

            if (displayMenu) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { displayMenu = false }
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
