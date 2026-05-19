package com.example.andespace.ui.friends

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.ui.main.AssetIcon

val friendStatusLabels: Map<UserStatus, String> = mapOf(
    UserStatus.INCOGNITO to "Do not disturb",
    UserStatus.BUSY to "Busy",
    UserStatus.EXERCISING to "Exercising",
    UserStatus.FREE to "Free",
    UserStatus.HANGING_OUT to "Hanging out",
    UserStatus.AT_HOME to "At home",
    UserStatus.LUNCHING to "Lunching"
)

fun statusLabel(status: UserStatus): String =
    friendStatusLabels[status] ?: status.value.replace('_', ' ').replaceFirstChar { it.uppercase() }

fun statusIconVector(status: UserStatus): ImageVector = when (status) {
    UserStatus.INCOGNITO -> Icons.Default.VisibilityOff
    UserStatus.BUSY -> Icons.Default.Laptop
    UserStatus.EXERCISING -> Icons.Default.FitnessCenter
    UserStatus.FREE -> Icons.Default.Weekend
    UserStatus.HANGING_OUT -> Icons.Default.Groups
    UserStatus.AT_HOME -> Icons.Default.Home
    UserStatus.LUNCHING -> Icons.Default.Restaurant
}

@Composable
fun FriendStatusIcon(
    status: UserStatus,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (status == UserStatus.FREE) {
        AssetIcon(
            assetPath = "icons/user.svg",
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        androidx.compose.material3.Icon(
            imageVector = statusIconVector(status),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}
