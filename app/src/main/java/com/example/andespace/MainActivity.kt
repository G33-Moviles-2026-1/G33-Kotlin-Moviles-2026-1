package com.example.andespace
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.login.LoginScreen
import com.example.andespace.ui.login.RegisterScreen
import com.example.andespace.ui.theme.AndeSpaceTheme
import com.example.andespace.ui.viewmodel.MainViewModel

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
