package com.example.andespace.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.main.AssetIcon

@Composable
fun AndeSpaceBottomBar(
    currentDestination: AppDestinations,
    onDestinationChanged: (AppDestinations) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        listOf(
            AppDestinations.CLASSROOMS,
            AppDestinations.FAVORITES,
            AppDestinations.BOOKINGS,
            AppDestinations.SCHEDULE
        ).forEach { destination ->
            val isSelected = destination == currentDestination

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationChanged(destination) },
                icon = {
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 2f else 1.5f,
                        label = "iconScale"
                    )
                    if (destination.assetIconPath != null) {
                        AssetIcon(
                            assetPath = destination.assetIconPath,
                            contentDescription = destination.label,
                            modifier = Modifier.scale(iconScale)
                        )
                    } else if (destination.icon != null) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.scale(iconScale)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}