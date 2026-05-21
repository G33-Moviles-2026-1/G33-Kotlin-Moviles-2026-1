package com.example.andespace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.andespace.ui.notifications.NotificationUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndeSpaceTopBar(
    isLoggedIn: Boolean,
    isMenuExpanded: Boolean,
    isNotificationsPopupExpanded: Boolean = false,
    notifications: List<NotificationUiModel> = emptyList(),
    unreadNotificationsCount: Int = 0,
    onAccountClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onDismissNotificationsPopup: () -> Unit = {},
    onMarkAsRead: (String) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogOut: () -> Unit,
    onProfileClick: () -> Unit = {},
    onFriendsClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Notifications bell (visible when logged in)
            if (isLoggedIn) {
                Box {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge {
                                    Text(
                                        text = if (unreadNotificationsCount > 99) "99+" else "$unreadNotificationsCount"
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.scale(1.5f)
                            )
                        }
                    }

                    if (isNotificationsPopupExpanded) {
                        NotificationsPopup(
                            notifications = notifications,
                            onDismiss = onDismissNotificationsPopup,
                            onMarkAsRead = onMarkAsRead,
                            onMarkAllAsRead = onMarkAllAsRead
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.size(48.dp))
            }

            Text(
                text = "AndeSpace",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Right: Person icon with dropdown menu
            Box {
                IconButton(onClick = onAccountClick) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.scale(1.5f)
                    )
                }

                if (isMenuExpanded) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(x = -20, y = 140),
                        onDismissRequest = onDismissMenu,
                        properties = PopupProperties(focusable = true)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .size(16.dp)
                                    .offset(y = 8.dp)
                                    .rotate(45f)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (!isLoggedIn) {
                                        CustomYellowButton("Log in", onLoginClick)
                                        CustomYellowButton("Register", onRegisterClick)
                                    } else {
                                        CustomYellowButton("Profile", onProfileClick)
                                        CustomYellowButton("Settings", onSettingsClick)
                                        CustomYellowButton("Friends", onFriendsClick)
                                        CustomYellowButton("Log out", onLogOut)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsPopup(
    notifications: List<NotificationUiModel>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(x = 0, y = 140),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(16.dp)
                    .offset(y = 8.dp)
                    .rotate(45f)
                    .background(MaterialTheme.colorScheme.background)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (notifications.any { !it.isRead }) {
                            TextButton(onClick = onMarkAllAsRead) {
                                Text(
                                    text = "Mark all",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Divider()
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No notifications yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                            items(notifications, key = { it.id }) { notif ->
                                NotificationPopupItem(
                                    notification = notif,
                                    onMarkAsRead = onMarkAsRead
                                )
                                if (notif != notifications.last()) {
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPopupItem(
    notification: NotificationUiModel,
    onMarkAsRead: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !notification.isRead) { onMarkAsRead(notification.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = if (notification.isRead)
                MaterialTheme.colorScheme.outline
            else
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "${notification.friendUsername} booked ${notification.roomId}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${notification.date} · ${notification.startTime} - ${notification.endTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
