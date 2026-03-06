package com.example.andespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.theme.AndeSpaceTheme
import com.example.andespace.ui.viewmodel.MainViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.ImageLoader
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
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AndeSpaceTopBar(
                    onHistoryClick = { viewModel.onHistoryClick() },
                    onAccountClick = { viewModel.onAccountClick() }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Greeting(
                name = if (uiState.isLoading) "Loading..." else uiState.currentDestination.label,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AndeSpaceTopBar(
    onHistoryClick: () -> Unit,
    onAccountClick: () -> Unit
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
            IconButton(onClick = onAccountClick) {
                AssetIcon(
                    assetPath = "icons/user.svg",
                    contentDescription = "Account",
                    modifier = Modifier.scale(1.5f)
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
