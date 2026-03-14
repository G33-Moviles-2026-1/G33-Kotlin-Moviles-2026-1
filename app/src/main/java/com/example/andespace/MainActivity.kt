package com.example.andespace
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.login.LoginScreen
import com.example.andespace.ui.login.RegisterScreen
import com.example.andespace.ui.theme.AndeSpaceTheme
import com.example.andespace.ui.viewmodel.MainViewModel
import androidx.compose.ui.platform.LocalContext

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

@PreviewScreenSizes
@Composable
fun AndeSpaceApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var userMenuExpanded by remember { mutableStateOf(false) }

    if (!uiState.isLoggedIn) {
        LoginScreen(onBackToApp = viewModel::onLogin)
        return
    }

    val myItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )

    NavigationSuiteScaffold(
        containerColor = Color.Transparent,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationBarContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val isSelected = destination == uiState.currentDestination

                item(
                    icon = {
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.5f else 1.1f,
                            label = "iconScale"
                        )
                        AssetIcon(
                            assetPath = destination.assetIconPath,
                            contentDescription = destination.label,
                            modifier = Modifier.scale(iconScale)
                        )
                    },
                    selected = isSelected,
                    onClick = { viewModel.onDestinationChanged(destination) },
                    colors = myItemColors
                )
                if(destination.label == "Classrooms" || destination.label == "Favorites" || destination.label == "Bookings" || destination.label == "Schedule"){
                    item(
                        icon = {
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.5f else 1.1f,
                                label = "iconScale"
                            )
                            Icon(
                                destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.scale(iconScale)
                            )
                        },
                        selected = isSelected,
                        onClick = { viewModel.onDestinationChanged(destination) },
                        colors = myItemColors
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                AndeSpaceTopBar(
                    onHistoryClick = { viewModel.onHistoryClick() },
                    userMenuExpanded = userMenuExpanded,
                    onUserMenuExpand = { userMenuExpanded = true },
                    onUserMenuDismiss = { userMenuExpanded = false },
                    onLogOut = {
                        viewModel.onLogOut()
                        userMenuExpanded = false
                    }
                    isMenuExpanded = uiState.isUserMenuExpanded,
                    onAccountClick = { viewModel.onAccountClick() },
                    onDismissMenu = { viewModel.onDismissMenu() },
                    onLoginClick = {
                        viewModel.onDestinationChanged(AppDestinations.LOGIN)
                        viewModel.onDismissMenu()
                    },
                    onRegisterClick = {
                        viewModel.onDestinationChanged(AppDestinations.REGISTER)
                        viewModel.onDismissMenu()
                    },
                    onHistoryClick = { viewModel.onHistoryClick() }
                )
            }
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            if (uiState.currentDestination == AppDestinations.HISTORY) {
                HistoryScreen(modifier = contentModifier)
            } else {
                Greeting(
                    name = if (uiState.isLoading) "Loading..." else uiState.currentDestination.label,
                    modifier = contentModifier
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onBackToApp: () -> Unit) {
    BackHandler { onBackToApp() }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {}
}

@Composable
fun AndeSpaceTopBar(
    onHistoryClick: () -> Unit,
    userMenuExpanded: Boolean,
    onUserMenuExpand: () -> Unit,
    onUserMenuDismiss: () -> Unit,
    onLogOut: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                when (uiState.currentDestination) {
                    AppDestinations.LOGIN -> {
                        LoginScreen(
                            uiState = uiState,
                            onUserChange = { viewModel.onUserChange(it) },
                            onPasswordChange = { viewModel.onPasswordChange(it) },
                            onLoginClick = { viewModel.onLoginExecute() }
                        )
                    }
                    AppDestinations.REGISTER -> {
                        RegisterScreen(
                            uiState = uiState,
                            onEmailChange = { viewModel.onUserChange(it) },
                            onPasswordChange = { viewModel.onPasswordChange(it) },
                            onLoginClick = { viewModel.onLoginExecute() }
                        )
                    }

                    else -> Greeting(name = uiState.currentDestination.label)
                }
            }
        }
    }
}
    @Composable
    fun AndeSpaceTopBar(
        isMenuExpanded: Boolean,
        onAccountClick: () -> Unit,
        onDismissMenu: () -> Unit,
        onLoginClick: () -> Unit,
        onRegisterClick: () -> Unit,
        onHistoryClick: () -> Unit,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        ) {
            IconButton(onClick = onHistoryClick) {
                AssetIcon(
                    assetPath = "icons/history.svg",
                    contentDescription = "History",
                    modifier = Modifier.scale(1.5f)
                )
            }
            Text(
                text = "AndeSpace",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box {
                IconButton(onClick = onUserMenuExpand) {
                    AssetIcon(
                        assetPath = "icons/user.svg",
                        contentDescription = "Account",
                        modifier = Modifier.scale(1.5f)
                    )
                }
                DropdownMenu(
                    expanded = userMenuExpanded,
                    onDismissRequest = onUserMenuDismiss
                ) {
                    Button(
                        onClick = {
                            onLogOut()
                            onUserMenuDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log out")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.scale(1.5f)
                    )
                }
                Text(
                    text = "AndeSpace",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box {
                    IconButton(onClick = onAccountClick) {
                        Icon(Icons.Default.Person, contentDescription = "Account")
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = onDismissMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log In") },
                            onClick = {
                                onDismissMenu()
                                onLoginClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Register") },
                            onClick = {
                                onDismissMenu()
                                onRegisterClick()
                            }
                        )
                    }
                }
            }
        }
    }

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {}
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
    Text(
        text = "Section: $name",
        modifier = modifier
    )
}
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Section: $name",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        AndeSpaceTheme {
            Greeting("Android")
        }
    }
